<?php

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$host = "localhost";
$db   = "gamestore_db";
$user = "root";
$pass = "";

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    die(json_encode([
        "success" => false,
        "message" => "Database connection failed"
    ]));
}

// lấy user_id từ query
$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

if ($user_id == 0) {
    echo json_encode([
        "success" => false,
        "message" => "Missing user_id"
    ]);
    exit;
}

$sql = "
SELECT 
    l.id,
    l.user_id,
    l.game_id,
    l.purchase_date,
    l.is_favorite,
    l.playtime_minutes,
    l.last_played_at,

    g.title,
    g.genre,
    g.price,
    g.thumbnail_url

FROM libraries l
JOIN games g ON l.game_id = g.id
WHERE l.user_id = ?
ORDER BY l.purchase_date DESC
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $user_id);
$stmt->execute();

$result = $stmt->get_result();

$libraries = [];

while ($row = $result->fetch_assoc()) {
    $libraries[] = $row;
}

echo json_encode([
    "success" => true,
    "data" => $libraries
]);

$conn->close();
?>