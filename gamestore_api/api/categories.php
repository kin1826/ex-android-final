<?php
require_once '../config/db.php';
$db = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$body = getBody();
$adminId = (int)($body['adminId'] ?? $_GET['adminId'] ?? 0);

// Hàm kiểm tra quyền admin
function isAdmin($db, $id) {
    if ($id <= 0) return false;
    $res = $db->query("SELECT is_admin FROM users WHERE id = $id");
    $u = $res->fetch_assoc();
    return (bool)($u['is_admin'] ?? 0);
}

// GET /api/categories.php (Lấy danh sách)
if ($method === 'GET') {
    $res = $db->query("SELECT * FROM categories ORDER BY name ASC");
    $list = [];
    while($row = $res->fetch_assoc()) {
        $list[] = [
            'id' => (int)$row['id'],
            'name' => $row['name'],
            'iconEmoji' => $row['icon'] ?? '🎮'
        ];
    }
    sendJSON(['success' => true, 'data' => $list]);
}

// POST /api/categories.php (Thêm mới)
if ($method === 'POST') {
    $adminIdFromQuery = (int)($_GET['adminId'] ?? 0);
    $finalAdminId = $adminId > 0 ? $adminId : $adminIdFromQuery;

    if (!isAdmin($db, $finalAdminId)) sendJSON(['success' => false, 'message' => 'Từ chối truy cập (Admin ID: ' . $finalAdminId . ')'], 403);

    $name = $db->real_escape_string($body['name'] ?? '');
    $icon = $db->real_escape_string($body['iconEmoji'] ?? '🎮');

    $stmt = $db->prepare("INSERT INTO categories (name, icon) VALUES (?, ?)");
    $stmt->bind_param('ss', $name, $icon);

    if ($stmt->execute()) sendJSON(['success' => true, 'message' => 'Đã thêm thể loại']);
    else sendJSON(['success' => false, 'message' => 'Lỗi DB: ' . $db->error], 500);
}

// DELETE /api/categories.php?id=1&adminId=1 (Xóa)
if ($method === 'DELETE') {
    if (!isAdmin($db, $adminId)) sendJSON(['success' => false, 'message' => 'Từ chối truy cập'], 403);
    $id = (int)($_GET['id'] ?? 0);

    if ($db->query("DELETE FROM categories WHERE id = $id")) {
        sendJSON(['success' => true, 'message' => 'Đã xóa thể loại']);
    } else {
        sendJSON(['success' => false, 'message' => 'Lỗi xóa: Có thể đang có game thuộc thể loại này'], 500);
    }
}
?>