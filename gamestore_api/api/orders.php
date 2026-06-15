<?php
require_once '../config/db.php';

$db     = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$uri    = $_SERVER['REQUEST_URI'];
$parts  = explode('/', trim(parse_url($uri, PHP_URL_PATH), '/'));
$body   = getBody();

// PATCH /api/orders/{id}/cancel
if ($method === 'PATCH' && in_array('cancel', $parts)) {
    $id = (int)$parts[array_search('cancel', $parts) - 1];
    $db->query("UPDATE orders SET status='CANCELLED' WHERE id=$id");
    sendJSON(['success' => true, 'message' => 'Đã hủy đơn hàng']);
}

// POST /api/orders
if ($method === 'POST') {
    $userId        = (int)($body['userId']        ?? 0);
    $items         = $body['items']               ?? [];
    $paymentMethod = $body['paymentMethod']        ?? 'WALLET';
    $note          = $db->real_escape_string($body['note'] ?? '');

    if (empty($items))
        sendJSON(['success' => false, 'message' => 'Giỏ hàng trống'], 400);

    $subtotal   = 0;
    $orderItems = [];

    // Tính tổng + lấy thông tin từng game
    foreach ($items as $item) {
        $gameId   = (int)$item['gameId'];
        $quantity = (int)$item['quantity'];
        $res      = $db->query("SELECT * FROM games WHERE id = $gameId");
        $game     = $res->fetch_assoc();

        if (!$game)
            sendJSON(['success' => false, 'message' => "Không tìm thấy game ID $gameId"], 400);

        $finalPrice = $game['discount_percent'] > 0
            ? $game['price'] * (1 - $game['discount_percent'] / 100)
            : (float)$game['price'];

        $subtotal += $finalPrice * $quantity;
        $orderItems[] = ['game' => $game, 'price' => $finalPrice, 'quantity' => $quantity];
    }

    // --- LOGIC TRỪ TIỀN VÍ ---
    if ($paymentMethod === 'WALLET') {
        // Kiểm tra tên cột
        $col = 'wallet_balance';
        $c = $db->query("SHOW COLUMNS FROM users LIKE 'wallet_balence'");
        if ($c->num_rows > 0) $col = 'wallet_balence';

        $resUser = $db->query("SELECT $col FROM users WHERE id = $userId");
        $user = $resUser->fetch_assoc();

        if (!$user || $user[$col] < $subtotal) {
            sendJSON(['success' => false, 'message' => 'Số dư ví không đủ. Vui lòng nạp thêm tiền.'], 400);
        }

        // Trừ tiền
        $db->query("UPDATE users SET $col = $col - $subtotal WHERE id = $userId");
    }

    // Tạo đơn hàng
    $stmt = $db->prepare("INSERT INTO orders (user_id,subtotal,discount,total,status,payment_method,note) VALUES (?,?,0,?,'COMPLETED',?,?)");
    $stmt->bind_param('iddss', $userId, $subtotal, $subtotal, $paymentMethod, $note);
    $stmt->execute();
    $orderId = $db->insert_id;

    // Lưu từng item
    $resultItems = [];
    foreach ($orderItems as $oi) {
        $gid = $oi['game']['id'];
        $p   = $oi['price'];
        $q   = $oi['quantity'];
        $db->query("INSERT INTO order_items (order_id,game_id,price,quantity) VALUES ($orderId,$gid,$p,$q)");

        $resultItems[] = [
            'gameId'        => (int)$gid,
            'gameTitle'     => $oi['game']['title'],
            'gameThumbnail' => $oi['game']['thumbnail_url'] ?? '',
            'price'         => $p,
            'quantity'      => $q,
        ];
    }

    sendJSON(['success' => true, 'data' => [
        'id'            => $orderId,
        'userId'        => $userId,
        'subtotal'      => $subtotal,
        'discount'      => 0,
        'total'         => $subtotal,
        'status'        => 'COMPLETED',
        'paymentMethod' => $paymentMethod,
        'paymentStatus' => 'PAID',
        'note'          => $note,
        'createdAt'     => date('c'),
        'items'         => $resultItems,
    ]], 201);
}

// GET /api/orders?userId=1
if ($method === 'GET') {
    $userId = (int)($_GET['userId'] ?? 0);
    $res    = $db->query("
        SELECT o.*, oi.id as item_id, oi.game_id, oi.price as item_price, oi.quantity,
               g.title as game_title, g.thumbnail_url
        FROM orders o
        LEFT JOIN order_items oi ON o.id = oi.order_id
        LEFT JOIN games g ON oi.game_id = g.id
        WHERE o.user_id = $userId
        ORDER BY o.created_at DESC
    ");

    $map = [];
    while ($row = $res->fetch_assoc()) {
        $oid = $row['id'];
        if (!isset($map[$oid])) {
            $map[$oid] = [
                'id'            => $oid,
                'userId'        => (int)$row['user_id'],
                'subtotal'      => (float)$row['subtotal'],
                'discount'      => (float)$row['discount'],
                'total'         => (float)$row['total'],
                'status'        => $row['status'],
                'paymentMethod' => $row['payment_method'],
                'note'          => $row['note'] ?? '',
                'createdAt'     => $row['created_at'],
                'items'         => [],
            ];
        }
        if ($row['item_id']) {
            $map[$oid]['items'][] = [
                'gameId'        => (int)$row['game_id'],
                'gameTitle'     => $row['game_title'] ?? '',
                'gameThumbnail' => $row['thumbnail_url'] ?? '',
                'price'         => (float)$row['item_price'],
                'quantity'      => (int)$row['quantity'],
            ];
        }
    }
    sendJSON(['success' => true, 'data' => array_values($map)]);
}
?>