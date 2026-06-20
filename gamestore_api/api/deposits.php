<?php
require_once '../config/db.php';

$db = getDB();
$method = $_SERVER['REQUEST_METHOD'];
$body = getBody();

// ── GET: Lấy danh sách yêu cầu nạp ──
if ($method === 'GET') {
    $userId = (int)($_GET['userId'] ?? 0);
    $isAdminView = isset($_GET['adminId']);
    $adminId = (int)($_GET['adminId'] ?? 0);

    if ($isAdminView) {
        // Kiểm tra quyền Admin
        $resCheck = $db->query("SELECT is_admin FROM users WHERE id = $adminId");
        $u = $resCheck->fetch_assoc();
        if (!$u || !$u['is_admin']) sendJSON(['success' => false, 'message' => 'Bạn không có quyền'], 403);

        $sql = "SELECT d.*, u.display_name, u.email
                FROM deposits d
                JOIN users u ON d.user_id = u.id
                ORDER BY d.created_at DESC";
    } else {
        if ($userId <= 0) sendJSON(['success' => false, 'message' => 'Thiếu userId'], 400);
        $sql = "SELECT * FROM deposits WHERE user_id = $userId ORDER BY created_at DESC";
    }

    $res = $db->query($sql);
    $items = [];
    while ($row = $res->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        $row['user_id'] = (int)$row['user_id'];
        $row['amount'] = (float)$row['amount'];
        $items[] = $row;
    }
    sendJSON(['success' => true, 'data' => $items]);
}

// ── POST: Tạo yêu cầu nạp tiền (User) ──
if ($method === 'POST' && !isset($body['action'])) {
    $userId = (int)($body['userId'] ?? 0);
    $amount = (float)($body['amount'] ?? 0);

    if ($userId <= 0 || $amount < 10000) {
        sendJSON(['success' => false, 'message' => 'Số tiền nạp tối thiểu là 10.000đ'], 400);
    }

    // Tạo mã nội dung chuyển khoản duy nhất: GS + UserId + Timestamp rút gọn
    $memo = "GS" . $userId . strtoupper(substr(uniqid(), -5));

    $stmt = $db->prepare("INSERT INTO deposits (user_id, amount, memo, status) VALUES (?, ?, ?, 'PENDING')");
    $stmt->bind_param("ids", $userId, $amount, $memo);

    if ($stmt->execute()) {
        sendJSON([
            'success' => true,
            'data' => [
                'id' => $db->insert_id,
                'amount' => $amount,
                'memo' => $memo,
                'status' => 'PENDING',
                'bank_info' => [
                    'account_name' => 'TRAN VAN BANG',
                    'account_number' => '0334082946',
                    'bank_name' => 'MB'
                ]
            ]
        ]);
    } else {
        sendJSON(['success' => false, 'message' => 'Lỗi: ' . $db->error], 500);
    }
}

// ── POST action=approve/reject: Duyệt nạp tiền (Admin) ──
if ($method === 'POST' && isset($body['action'])) {
    $adminId = (int)($body['adminId'] ?? 0);
    $depositId = (int)($body['depositId'] ?? 0);
    $action = $body['action']; // approve hoặc reject
    $adminNote = $db->real_escape_string($body['adminNote'] ?? '');

    // Kiểm tra quyền Admin
    $resCheck = $db->query("SELECT is_admin FROM users WHERE id = $adminId");
    $u = $resCheck->fetch_assoc();
    if (!$u || !$u['is_admin']) sendJSON(['success' => false, 'message' => 'Bạn không có quyền'], 403);

    // Lấy thông tin phiếu nạp
    $resD = $db->query("SELECT * FROM deposits WHERE id = $depositId AND status = 'PENDING'");
    $deposit = $resD->fetch_assoc();
    if (!$deposit) sendJSON(['success' => false, 'message' => 'Yêu cầu không tồn tại hoặc đã được xử lý'], 404);

    $userId = (int)$deposit['user_id'];
    $amount = (float)$deposit['amount'];

    if ($action === 'approve') {
        $db->begin_transaction();
        try {
            // 1. Cập nhật trạng thái phiếu nạp
            $db->query("UPDATE deposits SET status = 'APPROVED' WHERE id = $depositId");

            // 2. Cộng tiền vào ví User
            $db->query("UPDATE users SET wallet_balance = wallet_balance + $amount WHERE id = $userId");

            // 3. Thêm thông báo
            $title = "Nạp tiền thành công";
            $msg = "Số tiền " . number_format($amount) . "đ đã được cộng vào ví của bạn. Chúc bạn chơi game vui vẻ!";
            $stmtNoti = $db->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)");
            $stmtNoti->bind_param("iss", $userId, $title, $msg);
            $stmtNoti->execute();

            $db->commit();
            sendJSON(['success' => true, 'message' => 'Đã duyệt nạp tiền thành công']);
        } catch (Exception $e) {
            $db->rollback();
            sendJSON(['success' => false, 'message' => 'Lỗi hệ thống: ' . $e->getMessage()], 500);
        }
    } else {
        $db->query("UPDATE deposits SET status = 'REJECTED', admin_note = '$adminNote' WHERE id = $depositId");

        // Thêm thông báo bị từ chối
        $title = "Yêu cầu nạp tiền bị từ chối";
        $msg = "Yêu cầu nạp " . number_format($amount) . "đ của bạn đã bị từ chối.";
        if (!empty($adminNote)) {
            $msg .= " Lý do: " . $adminNote;
        } else {
            $msg .= " Vui lòng kiểm tra lại giao dịch hoặc liên hệ hỗ trợ.";
        }

        $stmtNoti = $db->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)");
        $stmtNoti->bind_param("iss", $userId, $title, $msg);
        $stmtNoti->execute();

        sendJSON(['success' => true, 'message' => 'Đã từ chối yêu cầu nạp tiền']);
    }
}
?>