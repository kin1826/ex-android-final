<?php
require_once '../config/db.php';
$db = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$adminId = (int)($_GET['adminId'] ?? 0);

// Kiểm tra quyền admin
$check = $db->query("SELECT is_admin FROM users WHERE id = $adminId");
$user = $check->fetch_assoc();
if (!$user || !($user['is_admin'])) {
    sendJSON(['success' => false, 'message' => 'Truy cập bị từ chối'], 403);
}

if ($method === 'GET') {
    // 1. Thống kê tổng quan
    $totalUsers = $db->query("SELECT COUNT(*) as c FROM users")->fetch_assoc()['c'];
    $totalOrders = $db->query("SELECT COUNT(*) as c FROM orders")->fetch_assoc()['c'];
    $totalRevenue = $db->query("SELECT SUM(total) as s FROM orders WHERE status = 'COMPLETED'")->fetch_assoc()['s'] ?? 0;

    // 2. Top 5 game bán chạy nhất
    $topGamesRes = $db->query("
        SELECT g.id, g.title, g.thumbnail_url, g.genre, g.price,
               SUM(oi.quantity) as salesCount
        FROM games g
        LEFT JOIN order_items oi ON g.id = oi.game_id
        LEFT JOIN orders o ON oi.order_id = o.id
        WHERE o.status = 'COMPLETED' OR oi.game_id IS NULL
        GROUP BY g.id
        ORDER BY salesCount DESC
        LIMIT 5
    ");

    $topGames = [];
    while ($row = $topGamesRes->fetch_assoc()) {
        $topGames[] = [
            'id' => (int)$row['id'],
            'title' => $row['title'],
            'thumbnailUrl' => $row['thumbnail_url'],
            'genre' => $row['genre'],
            'price' => (float)$row['price'],
            'salesCount' => (int)($row['salesCount'] ?? 0)
        ];
    }

    sendJSON([
        'success' => true,
        'data' => [
            'totalUsers' => (int)$totalUsers,
            'totalOrders' => (int)$totalOrders,
            'totalRevenue' => (float)$totalRevenue,
            'topGames' => $topGames
        ]
    ]);
}
?>