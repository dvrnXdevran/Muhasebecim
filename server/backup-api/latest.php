<?php
declare(strict_types=1);

require_once __DIR__ . '/bootstrap.php';

require_auth();

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    json_response(['success' => false, 'message' => 'Method not allowed'], 405);
}

$deviceId = trim((string) ($_GET['device_id'] ?? ''));
if ($deviceId === '') {
    json_response(['success' => false, 'message' => 'Missing query: device_id'], 400);
}

$stmt = db()->prepare(
    'SELECT payload_json
     FROM muhasebecim_backups
     WHERE device_id = :device_id
     ORDER BY id DESC
     LIMIT 1'
);
$stmt->execute([':device_id' => $deviceId]);
$row = $stmt->fetch();

if (!$row) {
    json_response([
        'success' => true,
        'hasBackup' => false,
        'backup' => null,
    ]);
}

$backup = json_decode((string) $row['payload_json'], true);
if (!is_array($backup)) {
    json_response(['success' => false, 'message' => 'Stored backup is invalid'], 500);
}

json_response([
    'success' => true,
    'hasBackup' => true,
    'backup' => $backup,
]);

