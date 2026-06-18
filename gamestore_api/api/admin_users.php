<?php
require_once '../config/db.php';
$db = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$body = getBody();

// 1. Kiểm tra quyền Admin
$adminId = (int)($body['adminId'] ?? $_GET['adminId'] ?? 0);
function checkAdminPermission($db, $id) {
    if ($id <= 0) return false;
    $res = $db->query("SELECT is_admin FROM users WHERE id = $id");
    $u = $res->fetch_assoc();
    return (bool)($u['is_admin'] ?? 0);
}

if (!checkAdminPermission($db, $adminId)) {
    sendJSON(['success' => false, 'message' => 'Bạn không có quyền truy cập'], 403);
}

// 2. Xử lý các yêu cầu
if ($method === 'GET') {
    // Lấy danh sách người dùng (kèm tìm kiếm)
    $search = $db->real_escape_string($_GET['q'] ?? '');
    $where = "1=1";
    if ($search) {
        $where .= " AND (username LIKE '%$search%' OR email LIKE '%$search%' OR display_name LIKE '%$search%')";
    }

    $res = $db->query("SELECT id, username, email, display_name as displayName, phone, wallet_balance as walletBalance, points, membership_level as membershipLevel, is_admin as isAdmin, is_active as isActive FROM users WHERE $where ORDER BY id DESC");

    $users = [];
    while($row = $res->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        $row['walletBalance'] = (float)$row['walletBalance'];
        $row['points'] = (int)$row['points'];
        $row['isAdmin'] = (bool)$row['isAdmin'];
        $row['isActive'] = (bool)$row['isActive'];
        $users[] = $row;
    }
    sendJSON(['success' => true, 'data' => $users]);
}

if ($method === 'POST') {
    $action = $_GET['action'] ?? '';
    $userId = (int)($body['userId'] ?? 0);
    if ($userId <= 0) sendJSON(['success' => false, 'message' => 'ID người dùng không hợp lệ'], 400);

    // -- Nạp/Trừ tiền --
    if ($action === 'update_wallet') {
        $amount = (float)($body['amount'] ?? 0);
        if ($db->query("UPDATE users SET wallet_balance = wallet_balance + ($amount) WHERE id = $userId")) {
            sendJSON(['success' => true, 'message' => 'Đã cập nhật số dư ví']);
        }
    }

    // -- Reset mật khẩu (về 123456) --
    if ($action === 'reset_password') {
        $newPass = password_hash('123456', PASSWORD_DEFAULT);
        if ($db->query("UPDATE users SET password_hash = '$newPass' WHERE id = $userId")) {
            sendJSON(['success' => true, 'message' => 'Mật khẩu đã đặt lại về: 123456']);
        }
    }

    // -- Đổi quyền/Trạng thái --
    if ($action === 'update_status') {
        $isAdmin = isset($body['isAdmin']) ? (int)$body['isAdmin'] : null;
        $isActive = isset($body['isActive']) ? (int)$body['isActive'] : null;

        $updates = [];
        if ($isAdmin !== null) $updates[] = "is_admin = $isAdmin";
        if ($isActive !== null) $updates[] = "is_active = $isActive";

        if (!empty($updates)) {
            $sql = "UPDATE users SET " . implode(', ', $updates) . " WHERE id = $userId";
            if ($db->query($sql)) sendJSON(['success' => true, 'message' => 'Đã cập nhật thông tin']);
        }
    }

    sendJSON(['success' => false, 'message' => 'Hành động không hợp lệ'], 400);
}
?>