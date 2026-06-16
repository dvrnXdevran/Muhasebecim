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
    'SELECT id, device_id, created_at, app_version, database_version, inserted_at
     FROM muhasebecim_backups
     WHERE device_id = :device_id
     ORDER BY id DESC
     LIMIT 10'
);
$stmt->execute([':device_id' => $deviceId]);

json_response([
    'success' => true,
    'backups' => $stmt->fetchAll(),
]);

