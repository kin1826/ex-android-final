<?php
require_once '../config/db.php';

$db     = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$uri    = $_SERVER['REQUEST_URI'];

$parts  = explode('/', trim(parse_url($uri, PHP_URL_PATH), '/'));
$lastPart = end($parts);

$action = $_GET['action'] ?? $lastPart;
$idFromQuery = $_GET['id'] ?? 0;

// GET Single Game
if ($method === 'GET' && ($idFromQuery > 0 || is_numeric($action))) {
    $id  = $idFromQuery > 0 ? (int)$idFromQuery : (int)$action;
    $userId = (int)($_GET['userId'] ?? 0);
    $res = $db->query("SELECT * FROM games WHERE id = $id LIMIT 1");
    $row = $res->fetch_assoc();
    if (!$row) sendJSON(['success' => false, 'message' => 'Không tìm thấy game'], 404);
    $gameData = formatGame($row);
    sendJSON(['success' => true, 'data' => $gameData]);
}

// GET Featured
if ($method === 'GET' && $action === 'featured') {
    $genre = $db->real_escape_string($_GET['genre'] ?? '');
    $where = "is_featured = 1";
    if ($genre) $where .= " AND genre = '$genre'";
    $res = $db->query("SELECT * FROM games WHERE $where ORDER BY rating DESC");
    sendJSON(['success' => true, 'data' => ['items' => fetchAll($res)]]);
}

// GET Hot Deals
if ($method === 'GET' && $action === 'hot-deals') {
    $genre = $db->real_escape_string($_GET['genre'] ?? '');
    $where = "is_hot = 1";
    if ($genre) $where .= " AND genre = '$genre'";
    $res = $db->query("SELECT * FROM games WHERE $where ORDER BY discount_percent DESC");
    sendJSON(['success' => true, 'data' => ['items' => fetchAll($res)]]);
}

// GET New Releases
if ($method === 'GET' && $action === 'new-releases') {
    $genre = $db->real_escape_string($_GET['genre'] ?? '');
    $where = "is_new = 1";
    if ($genre) $where .= " AND genre = '$genre'";
    $res = $db->query("SELECT * FROM games WHERE $where ORDER BY created_at DESC");
    sendJSON(['success' => true, 'data' => ['items' => fetchAll($res)]]);
}

// GET Search & Filtered List
if ($method === 'GET') {
    $where    = '1=1';
    $genre    = $db->real_escape_string($_GET['genre']  ?? '');
    $search   = $db->real_escape_string($_GET['search'] ?? '');
    $platform = $db->real_escape_string($_GET['platform'] ?? '');
    $minPrice = isset($_GET['minPrice']) ? (float)$_GET['minPrice'] : -1;
    $maxPrice = isset($_GET['maxPrice']) ? (float)$_GET['maxPrice'] : -1;
    $sortBy   = $_GET['sortBy'] ?? 'newest';
    $onlyDiscounted = isset($_GET['onlyDiscounted']) && ($_GET['onlyDiscounted'] === 'true' || $_GET['onlyDiscounted'] == 1);

    if ($genre)    $where .= " AND genre = '$genre'";
    if ($search)   $where .= " AND (title LIKE '%$search%' OR genre LIKE '%$search%')";
    if ($platform) $where .= " AND platforms LIKE '%$platform%'";
    if ($minPrice >= 0) $where .= " AND price >= $minPrice";
    if ($maxPrice >= 0) $where .= " AND price <= $maxPrice";
    if ($onlyDiscounted) $where .= " AND discount_percent > 0";

    $order = "created_at DESC";
    if ($sortBy === 'price_asc')  $order = "price ASC";
    elseif ($sortBy === 'price_desc') $order = "price DESC";
    elseif ($sortBy === 'rating')     $order = "rating DESC";

    $page   = max(0, (int)($_GET['page'] ?? 0));
    $size   = min(50, (int)($_GET['pageSize'] ?? 20));
    $offset = $page * $size;

    $total = $db->query("SELECT COUNT(*) as c FROM games WHERE $where")->fetch_assoc()['c'];
    $res   = $db->query("SELECT * FROM games WHERE $where ORDER BY $order LIMIT $size OFFSET $offset");

    sendJSON(['success' => true, 'data' => [
        'items'    => fetchAll($res),
        'total'    => (int)$total,
        'page'     => $page,
        'pageSize' => $size,
    ]]);
}

function fetchAll($result) {
    $rows = [];
    if ($result) { while ($row = $result->fetch_assoc()) $rows[] = formatGame($row); }
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
        'stock'           => (int)$row['stock'],
    ];
}
?>