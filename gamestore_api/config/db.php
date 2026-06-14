<?php
// 1. Cấu hình mặc định (Dành cho máy XAMPP của bạn bạn)
$db_host = '127.0.0.1';
$db_user = 'root';
$db_pass = '';
$db_name = 'gamestore_db';

// 2. Kiểm tra nếu có file db.local.php thì lấy cấu hình ở đó ghi đè vào
$localConfig = __DIR__ . '/db.local.php';
if (file_exists($localConfig)) {
    include $localConfig;
}

define('DB_HOST', $db_host);
define('DB_USER', $db_user);
define('DB_PASS', $db_pass);
define('DB_NAME', $db_name);

function getDB() {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
    $conn->set_charset('utf8mb4');

    if ($conn->connect_error) {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Lỗi kết nối database']);
        exit();
    }
    return $conn;
}

function sendJSON($data, $code = 200) {
    http_response_code($code);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit();
}

function getBody() {
    return json_decode(file_get_contents('php://input'), true) ?? [];
}

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(200); exit(); }
?>