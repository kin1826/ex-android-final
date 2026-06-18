<?php
require_once '../config/db.php';

$db = getDB();
$method = $_SERVER['REQUEST_METHOD'];

// ── GET: Lấy danh sách yêu thích ──
if ($method === 'GET') {
    $userId = (int)($_GET['userId'] ?? 0);
    if ($userId <= 0) sendJSON(['success' => false, 'message' => 'Thiếu userId'], 400);

    $sql = "
        SELECT g.*
        FROM wishlists w
        JOIN games g ON w.game_id = g.id
        WHERE w.user_id = $userId
        ORDER BY w.added_at DESC
    ";

    $res = $db->query($sql);
    $items = [];
    while ($row = $res->fetch_assoc()) {
        $items[] = formatGame($row);
    }

    sendJSON(['success' => true, 'data' => $items]);
}

// ── POST: Toggle yêu thích (Thêm/Xóa) ──
if ($method === 'POST') {
    $body = getBody();
    $userId = (int)($body['userId'] ?? 0);
    $gameId = (int)($body['gameId'] ?? 0);

    if ($userId <= 0 || $gameId <= 0) {
        sendJSON(['success' => false, 'message' => 'Thiếu dữ liệu'], 400);
    }

    // Kiểm tra xem đã có trong wishlist chưa
    $check = $db->query("SELECT * FROM wishlists WHERE user_id = $userId AND game_id = $gameId");

    if ($check && $check->num_rows > 0) {
        // Nếu có rồi thì xóa
        $db->query("DELETE FROM wishlists WHERE user_id = $userId AND game_id = $gameId");
        sendJSON(['success' => true, 'message' => 'Đã xóa khỏi danh sách yêu thích', 'data' => ['isFavorite' => false]]);
    } else {
        // Nếu chưa có thì thêm
        $db->query("INSERT INTO wishlists (user_id, game_id) VALUES ($userId, $gameId)");
        sendJSON(['success' => true, 'message' => 'Đã thêm vào danh sách yêu thích', 'data' => ['isFavorite' => true]]);
    }
}

// ── HELPERS ── (Copy từ games.php hoặc đưa vào common)
function formatGame($row) {
    return [
        'id'              => (int)$row['id'],
        'title'           => $row['title'],
        'description'     => $row['description'] ?? '',
        'price'           => (float)$row['price'],
        'originalPrice'   => (float)$row['original_price'],
        'discountPercent' => (int)$row['discount_percent'],
        'rating'          => (float)$row['rating'],
        'reviewCount'     => (int)$row['review_count'],
        'genre'           => $row['genre'] ?? '',
        'developer'       => $row['developer'] ?? '',
        'thumbnailUrl'    => $row['thumbnail_url'] ?? '',
        'bannerUrl'       => $row['banner_url'] ?? '',
        'isFeatured'      => (bool)$row['is_featured'],
        'isHot'           => (bool)$row['is_hot'],
        'isNew'           => (bool)$row['is_new'],
        'stock'           => (int)$row['stock'],
    ];
}
?>