<?php
require_once '../config/db.php';

$db = getDB();

// Lấy user_id từ query
$userId = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 0;

if ($userId <= 0) {
    sendJSON(["success" => false, "message" => "Thiếu user_id"], 400);
}

$sql = "
    SELECT
        l.id,
        l.user_id as userId,
        l.game_id as gameId,
        l.purchase_date as purchaseDate,
        l.is_favorite as isFavorite,
        l.playtime_minutes as playtimeMinutes,
        l.last_played_at as lastPlayedAt,
        COALESCE(g.title, '') as gameTitle,
        COALESCE(g.genre, '') as genre,
        COALESCE(g.price, 0) as price,
        COALESCE(g.thumbnail_url, '') as thumbnailUrl,
        COALESCE(g.description, '') as description,
        COALESCE(g.discount_percent, 0) as discountPercent
    FROM libraries l
    JOIN games g ON l.game_id = g.id
    WHERE l.user_id = ?
    ORDER BY l.purchase_date DESC
";

$stmt = $db->prepare($sql);
$stmt->bind_param("i", $userId);
$stmt->execute();
$result = $stmt->get_result();

$libraries = [];
while ($row = $result->fetch_assoc()) {
    $row['id']              = (int)$row['id'];
    $row['userId']          = (int)$row['userId'];
    $row['gameId']          = (int)$row['gameId'];
    $row['isFavorite']      = (bool)$row['isFavorite'];
    $row['playtimeMinutes'] = (int)$row['playtimeMinutes'];
    $row['price']           = (float)$row['price'];
    $row['discountPercent'] = (float)$row['discountPercent'];
    $libraries[] = $row;
}

sendJSON(["success" => true, "data" => $libraries]);
?>