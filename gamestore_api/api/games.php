<?php
require_once '../config/db.php';

$db     = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$uri    = $_SERVER['REQUEST_URI'];

// Lấy path cuối: /api/games/featured → "featured"
$parts  = explode('/', trim(parse_url($uri, PHP_URL_PATH), '/'));
$lastPart = end($parts);

// Ưu tiên lấy action từ Query String, nếu không có mới lấy từ Path
$action = $_GET['action'] ?? $lastPart;
$idFromQuery = $_GET['id'] ?? 0;

// ── GET /api/games/{id} ──────────────────────────────────────
if ($method === 'GET' && ($idFromQuery > 0 || is_numeric($action))) {
    $id  = $idFromQuery > 0 ? (int)$idFromQuery : (int)$action;
    $userId = (int)($_GET['userId'] ?? 0);

    $res = $db->query("SELECT * FROM games WHERE id = $id LIMIT 1");
    $row = $res->fetch_assoc();
    if (!$row) sendJSON(['success' => false, 'message' => 'Không tìm thấy game'], 404);

    // Kiểm tra xem user đã mua game này chưa
    $isOwned = false;
    if ($userId > 0) {
        $checkOwned = $db->query("
            SELECT oi.id
            FROM order_items oi
            JOIN orders o ON oi.order_id = o.id
            WHERE o.user_id = $userId AND oi.game_id = $id AND o.status = 'COMPLETED'
            LIMIT 1
        ");
        $isOwned = ($checkOwned && $checkOwned->num_rows > 0);
    }

    $gameData = formatGame($row);
    $gameData['isOwned'] = $isOwned;

    sendJSON(['success' => true, 'data' => $gameData]);
}

// ── GET /api/games/featured ──────────────────────────────────
if ($method === 'GET' && $action === 'featured') {
    $res = $db->query("SELECT * FROM games WHERE is_featured = 1 ORDER BY rating DESC");
    sendJSON(['success' => true, 'data' => ['items' => fetchAll($res)]]);
}

// ── GET /api/games/hot-deals ─────────────────────────────────
if ($method === 'GET' && $action === 'hot-deals') {
    $res = $db->query("SELECT * FROM games WHERE is_hot = 1 ORDER BY discount_percent DESC");
    sendJSON(['success' => true, 'data' => ['items' => fetchAll($res)]]);
}

// ── GET /api/games/new-releases ──────────────────────────────
if ($method === 'GET' && $action === 'new-releases') {
    $res = $db->query("SELECT * FROM games WHERE is_new = 1 ORDER BY created_at DESC");
    sendJSON(['success' => true, 'data' => ['items' => fetchAll($res)]]);
}

// ── GET /api/games/categories ────────────────────────────────
if ($method === 'GET' && $action === 'categories') {
    $res = $db->query("
        SELECT c.id, c.name, c.icon as iconEmoji,
               COUNT(g.id) as gameCount
        FROM categories c
        LEFT JOIN games g ON g.genre = c.name
        GROUP BY c.id, c.name, c.icon
    ");
    $list = [];
    while($row = $res->fetch_assoc()) {
        $list[] = [
            'id' => (int)$row['id'],
            'name' => $row['name'],
            'iconEmoji' => $row['iconEmoji'] ?? '🎮',
            'gameCount' => (int)$row['gameCount']
        ];
    }
    sendJSON(['success' => true, 'data' => $list]);
}

// ── GET /api/games/search?q= ─────────────────────────────────
if ($method === 'GET' && $action === 'search') {
    $q   = '%' . ($db->real_escape_string($_GET['q'] ?? '')) . '%';
    $res = $db->query("SELECT * FROM games WHERE title LIKE '$q' OR genre LIKE '$q' ORDER BY rating DESC");
    sendJSON(['success' => true, 'data' => ['items' => fetchAll($res)]]);
}

// ── GET /api/games (danh sách có lọc) ────────────────────────
if ($method === 'GET') {
    // ... (giữ nguyên code cũ)
    $where  = '1=1';
    $genre  = $db->real_escape_string($_GET['genre']  ?? '');
    $search = $db->real_escape_string($_GET['search'] ?? '');
    $page   = max(0, (int)($_GET['page'] ?? 0));
    $size   = min(50, (int)($_GET['pageSize'] ?? 20));
    $offset = $page * $size;

    if ($genre)  $where .= " AND genre = '$genre'";
    if ($search) $where .= " AND (title LIKE '%$search%' OR genre LIKE '%$search%')";

    $total = $db->query("SELECT COUNT(*) as c FROM games WHERE $where")->fetch_assoc()['c'];
    $res   = $db->query("SELECT * FROM games WHERE $where ORDER BY created_at DESC LIMIT $size OFFSET $offset");

    sendJSON(['success' => true, 'data' => [
        'items'    => fetchAll($res),
        'total'    => (int)$total,
        'page'     => $page,
        'pageSize' => $size,
    ]]);
}

// ── QUẢN TRỊ VIÊN (POST/PUT/DELETE) ───────────────────────────
$body = getBody();
$adminId = (int)($body['adminId'] ?? $_GET['adminId'] ?? 0);

// Hàm kiểm tra quyền Admin
function checkAdmin($db, $id) {
    if ($id <= 0) return false;
    $res = $db->query("SELECT is_admin FROM users WHERE id = $id");
    $u = $res->fetch_assoc();
    return (bool)($u['is_admin'] ?? 0);
}

// Thêm Game (POST)
if ($method === 'POST') {
    if (!checkAdmin($db, $adminId)) sendJSON(['success' => false, 'message' => 'Bạn không có quyền'], 403);

    $stmt = $db->prepare("INSERT INTO games (title, description, price, original_price, discount_percent, genre, developer, publisher, release_date, platforms, download_size, thumbnail_url, is_featured, is_hot, is_new) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
    $stmt->bind_param('ssddisssssssiii',
        $body['title'], $body['description'], $body['price'], $body['originalPrice'],
        $body['discountPercent'], $body['genre'], $body['developer'], $body['publisher'],
        $body['releaseDate'], $body['platforms'], $body['downloadSize'], $body['thumbnailUrl'],
        $body['isFeatured'], $body['isHot'], $body['isNew']
    );

    if ($stmt->execute()) sendJSON(['success' => true, 'message' => 'Thêm game thành công', 'id' => $db->insert_id]);
    else sendJSON(['success' => false, 'message' => 'Lỗi: ' . $db->error], 500);
}

// Sửa Game (PUT)
if ($method === 'PUT') {
    if (!checkAdmin($db, $adminId)) sendJSON(['success' => false, 'message' => 'Bạn không có quyền'], 403);
    $id = (int)$body['id'];

    $stmt = $db->prepare("UPDATE games SET title=?, description=?, price=?, original_price=?, discount_percent=?, genre=?, developer=?, publisher=?, release_date=?, platforms=?, download_size=?, thumbnail_url=?, is_featured=?, is_hot=?, is_new=? WHERE id=?");
    $stmt->bind_param('ssddisssssssiiii',
        $body['title'], $body['description'], $body['price'], $body['originalPrice'],
        $body['discountPercent'], $body['genre'], $body['developer'], $body['publisher'],
        $body['releaseDate'], $body['platforms'], $body['downloadSize'], $body['thumbnailUrl'],
        $body['isFeatured'], $body['isHot'], $body['isNew'], $id
    );

    if ($stmt->execute()) sendJSON(['success' => true, 'message' => 'Cập nhật thành công']);
    else sendJSON(['success' => false, 'message' => 'Lỗi: ' . $db->error], 500);
}

// Xóa Game (DELETE)
if ($method === 'DELETE') {
    if (!checkAdmin($db, $adminId)) sendJSON(['success' => false, 'message' => 'Bạn không có quyền'], 403);
    $id = (int)($_GET['id'] ?? 0);

    if ($db->query("DELETE FROM games WHERE id = $id")) sendJSON(['success' => true, 'message' => 'Đã xóa game']);
    else sendJSON(['success' => false, 'message' => 'Lỗi: ' . $db->error], 500);
}

// ── HELPERS ──────────────────────────────────────────────────
function fetchAll($result) {
    $rows = [];
    if ($result) {
        while ($row = $result->fetch_assoc()) $rows[] = formatGame($row);
    }
    return $rows;
}

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
        'publisher'       => $row['publisher'] ?? '',
        'releaseDate'     => $row['release_date'] ?? '',
        'platforms'       => $row['platforms'] ?? '',
        'downloadSize'    => $row['download_size'] ?? '',
        'thumbnailUrl'    => $row['thumbnail_url'] ?? '',
        'bannerUrl'       => $row['banner_url'] ?? '',
        'isFeatured'      => (bool)$row['is_featured'],
        'isHot'           => (bool)$row['is_hot'],
        'isNew'           => (bool)$row['is_new'],
        'isOwned'         => (bool)($row['isOwned'] ?? false),
        'stock'           => (int)$row['stock'],
    ];
}
?>