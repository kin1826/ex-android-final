<?php
require_once '../config/db.php';
$db = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$body = getBody();
$action = $_GET['action'] ?? '';

if ($method === 'POST' && $action === 'deposit') {
    $userId = (int)($body['userId'] ?? 0);
    $amount = (float)($body['amount'] ?? 0);

    if ($userId <= 0 || $amount <= 0) {
        sendJSON(['success' => false, 'message' => 'Dữ liệu không hợp lệ'], 400);
    }

    // Kiểm tra tên cột tồn tại trong DB (tránh lỗi sai chính tả balance/balence)
    $columnName = 'wallet_balance'; // mặc định
    $res = $db->query("DESCRIBE users");
    if ($res) {
        while($row = $res->fetch_assoc()) {
            if ($row['Field'] === 'wallet_balence') {
                $columnName = 'wallet_balence';
                break;
            }
        }
    }

    // Cập nhật số dư
    $stmt = $db->prepare("UPDATE users SET $columnName = $columnName + ? WHERE id = ?");
    $stmt->bind_param('di', $amount, $userId);

    if ($stmt->execute()) {
        $res = $db->query("SELECT * FROM users WHERE id = $userId");
        $user = $res->fetch_assoc();

        sendJSON([
            'success' => true,
            'message' => 'Nạp tiền thành công!',
            'data' => [
                'newBalance' => (float)($user[$columnName] ?? 0)
            ]
        ]);
    } else {
        sendJSON(['success' => false, 'message' => 'Lỗi cập nhật database: ' . $db->error], 500);
    }
}

// GET /api/users.php?action=profile&userId=1
if ($method === 'GET' && $action === 'profile') {
    $userId = (int)($_GET['userId'] ?? 0);
    $res = $db->query("SELECT * FROM users WHERE id = $userId");
    $user = $res->fetch_assoc();

    if (!$user) sendJSON(['success' => false, 'message' => 'User không tồn tại'], 404);

    // Kiểm tra tên cột
    $col = 'wallet_balance';
    $c = $db->query("SHOW COLUMNS FROM users LIKE 'wallet_balence'");
    if ($c->num_rows > 0) $col = 'wallet_balence';

    sendJSON(['success' => true, 'data' => [
        'id'              => (int)$user['id'],
        'username'        => $user['username'],
        'email'           => $user['email'],
        'displayName'     => $user['display_name'] ?? '',
        'phone'           => $user['phone'] ?? '',
        'avatarUrl'       => $user['avatar_url'] ?? '',
        'walletBalance'   => (float)$user[$col],
        'points'          => (int)$user['points'],
        'membershipLevel' => $user['membership_level'] ?? 'BRONZE',
    ]]);
}
?>