<?php
declare(strict_types=1);

require_once __DIR__ . '/bootstrap.php';

require_auth();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_response(['success' => false, 'message' => 'Method not allowed'], 405);
}

$snapshot = request_json();

$deviceId = require_string($snapshot, 'deviceId');
$createdAt = require_int($snapshot, 'createdAt');
$appVersion = require_string($snapshot, 'appVersion');
$databaseVersion = require_int($snapshot, 'databaseVersion');

if (!isset($snapshot['payload']) || !is_array($snapshot['payload'])) {
    json_response(['success' => false, 'message' => 'Missing field: payload'], 400);
}

$payloadJson = json_encode($snapshot, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
if ($payloadJson === false) {
    json_response(['success' => false, 'message' => 'Snapshot encode failed'], 400);
}

$pdo = db();
$stmt = $pdo->prepare(
    'INSERT INTO muhasebecim_backups (device_id, created_at, app_version, database_version, payload_json)
     VALUES (:device_id, :created_at, :app_version, :database_version, :payload_json)'
);
$stmt->execute([
    ':device_id' => $deviceId,
    ':created_at' => $createdAt,
    ':app_version' => $appVersion,
    ':database_version' => $databaseVersion,
    ':payload_json' => $payloadJson,
]);

cleanup_old_backups($pdo, $deviceId);

json_response([
    'success' => true,
    'message' => 'Backup saved',
    'id' => (int) $pdo->lastInsertId(),
]);

