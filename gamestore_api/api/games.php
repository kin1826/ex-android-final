<?php
require_once '../config/db.php';

$db     = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$uri    = $_SERVER['REQUEST_URI'];

// Lấy path cuối: /api/games/featured → "featured"
$parts  = explode('/', trim(parse_url($uri, PHP_URL_PATH), '/'));
$action = end($parts);   // featured | hot-deals | new-releases | search | categories | {id}

// ── GET /api/games/featured ──────────────────────────────────
if ($method === 'GET' && $action === 'featured') {
    $res = $db->query("SELECT * FROM games WHERE is_featured = 1 ORDER BY rating DESC");
    sendJSON(['success' => true, 'data' => fetchAll($res)]);
}

// ── GET /api/games/hot-deals ─────────────────────────────────
if ($method === 'GET' && $action === 'hot-deals') {
    $res = $db->query("SELECT * FROM games WHERE is_hot = 1 ORDER BY discount_percent DESC");
    sendJSON(['success' => true, 'data' => fetchAll($res)]);
}

// ── GET /api/games/new-releases ──────────────────────────────
if ($method === 'GET' && $action === 'new-releases') {
    $res = $db->query("SELECT * FROM games WHERE is_new = 1 ORDER BY created_at DESC");
    sendJSON(['success' => true, 'data' => fetchAll($res)]);
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
    sendJSON(['success' => true, 'data' => fetchAll($res)]);
}

// ── GET /api/games/search?q= ─────────────────────────────────
if ($method === 'GET' && $action === 'search') {
    $q   = '%' . ($db->real_escape_string($_GET['q'] ?? '')) . '%';
    $res = $db->query("SELECT * FROM games WHERE title LIKE '$q' OR genre LIKE '$q' ORDER BY rating DESC");
    sendJSON(['success' => true, 'data' => fetchAll($res)]);
}

// ── GET /api/games/{id} ──────────────────────────────────────
if ($method === 'GET' && is_numeric($action)) {
    $id  = (int)$action;
    $res = $db->query("SELECT * FROM games WHERE id = $id LIMIT 1");
    $row = $res->fetch_assoc();
    if (!$row) sendJSON(['success' => false, 'message' => 'Không tìm thấy game'], 404);
    sendJSON(['success' => true, 'data' => formatGame($row)]);
}

// ── GET /api/games (danh sách có lọc) ────────────────────────
if ($method === 'GET') {
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

// ── HELPERS ──────────────────────────────────────────────────
function fetchAll($result) {
    $rows = [];
    while ($row = $result->fetch_assoc()) $rows[] = formatGame($row);
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
        'thumbnailUrl'    => $row['thumbnail_url'] ?? '',
        'bannerUrl'       => $row['banner_url'] ?? '',
        'isFeatured'      => (bool)$row['is_featured'],
        'isHot'           => (bool)$row['is_hot'],
        'isNew'           => (bool)$row['is_new'],
        'stock'           => (int)$row['stock'],
    ];
}
?>