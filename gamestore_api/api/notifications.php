<?php
require_once '../config/db.php';

$db = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$body = getBody();

// ── GET: Lấy danh sách thông báo ──
if ($method === 'GET') {
    $userId = (int)($_GET['userId'] ?? 0);
    if ($userId <= 0) sendJSON(['success' => false, 'message' => 'Thiếu userId'], 400);

    // Lấy thông báo riêng của user + thông báo chung cho tất cả (user_id = 0)
    $sql = "SELECT * FROM notifications
            WHERE user_id = $userId OR user_id = 0
            ORDER BY created_at DESC LIMIT 50";

    $res = $db->query($sql);
    $items = [];
    while ($row = $res->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        $row['user_id'] = (int)$row['user_id'];
        $row['is_read'] = (bool)$row['is_read'];
        $items[] = $row;
    }
    sendJSON(['success' => true, 'data' => $items]);
}

// ── POST: Đánh dấu đã đọc ──
if ($method === 'POST') {
    $userId = (int)($body['userId'] ?? 0);
    $notiId = (int)($body['notificationId'] ?? 0); // Nếu notificationId = 0 thì đánh dấu đọc tất cả

    if ($userId <= 0) sendJSON(['success' => false, 'message' => 'Thiếu dữ liệu'], 400);

    if ($notiId > 0) {
        $db->query("UPDATE notifications SET is_read = 1 WHERE id = $notiId AND (user_id = $userId OR user_id = 0)");
    } else {
        $db->query("UPDATE notifications SET is_read = 1 WHERE user_id = $userId OR user_id = 0");
    }

    sendJSON(['success' => true, 'message' => 'Cập nhật thành công']);
}
?>