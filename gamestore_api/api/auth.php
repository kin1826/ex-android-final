<?php
require_once '../config/db.php';

$db     = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$uri    = $_SERVER['REQUEST_URI'];
$parts  = explode('/', trim(parse_url($uri, PHP_URL_PATH), '/'));
$action = $_GET['action'] ?? end($parts);

// Đọc body JSON
$rawBody = file_get_contents('php://input');
$body    = json_decode($rawBody, true) ?? [];

// ── ĐĂNG KÝ ─────────────────────────────────────────────────
if ($method === 'POST' && $action === 'register') {
    $username    = trim($body['username']    ?? '');
    $email       = trim($body['email']       ?? '');
    $password    = trim($body['password']    ?? '');
    $displayName = trim($body['displayName'] ?? $username);
    $phone       = trim($body['phone']       ?? '');

    if (!$username || !$email || !$password) {
        sendJSON(['success' => false, 'message' => 'Vui lòng điền đầy đủ thông tin'], 400);
    }

    $esc   = $db->real_escape_string($email);
    $check = $db->query("SELECT id FROM users WHERE email = '$esc'");
    if ($check->num_rows > 0) {
        sendJSON(['success' => false, 'message' => 'Email đã được đăng ký'], 400);
    }

    $hash = password_hash($password, PASSWORD_BCRYPT);
    $stmt = $db->prepare("INSERT INTO users (username, email, password_hash, display_name, phone) VALUES (?,?,?,?,?)");
    $stmt->bind_param('sssss', $username, $email, $hash, $displayName, $phone);
    $stmt->execute();
    $userId = $db->insert_id;

    $token = generateToken($userId, $email);
    sendJSON(['success' => true, 'data' => buildAuthResponse($db, $userId, $token)], 201);
}

// ── ĐĂNG NHẬP ───────────────────────────────────────────────
if ($method === 'POST' && $action === 'login') {
    $email    = trim($body['email']    ?? '');
    $password = trim($body['password'] ?? '');

    if (!$email || !$password) {
        sendJSON(['success' => false, 'message' => 'Vui lòng nhập email và mật khẩu'], 400);
    }

    $stmt = $db->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->bind_param('s', $email);
    $stmt->execute();
    $user = $stmt->get_result()->fetch_assoc();

    if (!$user || !password_verify($password, $user['password_hash'])) {
        sendJSON(['success' => false, 'message' => 'Email hoặc mật khẩu không đúng'], 401);
    }

    $token = generateToken($user['id'], $user['email']);
    sendJSON(['success' => true, 'data' => buildAuthResponse($db, $user['id'], $token)]);
}

// ── ĐĂNG XUẤT ───────────────────────────────────────────────
if ($method === 'POST' && $action === 'logout') {
    sendJSON(['success' => true, 'message' => 'Đăng xuất thành công']);
}

// ── HELPERS ─────────────────────────────────────────────────
function generateToken($userId, $email) {
    $secret  = 'GameStore_Secret_2024';
    $payload = "$userId|$email|" . time();
    return base64_encode($payload . '|' . hash('sha256', $payload . $secret));
}

function buildAuthResponse($db, $userId, $token) {
    $res  = $db->query("SELECT * FROM users WHERE id = $userId");
    $user = $res->fetch_assoc();
    return [
        'accessToken'  => $token,
        'refreshToken' => '',
        'expiresIn'    => (time() + 7 * 24 * 3600) * 1000,
        'userId'       => (int)$userId,
        'user'         => formatUser($user),
    ];
}

function formatUser($u) {
    return [
        'id'              => (int)$u['id'],
        'username'        => $u['username'],
        'email'           => $u['email'],
        'displayName'     => $u['display_name'] ?? '',
        'phone'           => $u['phone'] ?? '',
        'avatarUrl'       => $u['avatar_url'] ?? '',
        'walletBalance'   => (float)($u['wallet_balance'] ?? $u['wallet_balence'] ?? 0),
        'points'          => (int)$u['points'],
        'membershipLevel' => $u['membership_level'] ?? 'BRONZE',
        'isAdmin'         => (bool)($u['is_admin'] ?? 0),
    ];
}
?>