<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
header('X-Robots-Tag: noindex, nofollow, noarchive', true);
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');

require_once __DIR__ . '/config.php';

function json_response(array $payload, int $statusCode = 200): void
{
    http_response_code($statusCode);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

function require_auth(): void
{
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    if ($header === '' && function_exists('apache_request_headers')) {
        $headers = apache_request_headers();
        $header = $headers['Authorization'] ?? $headers['authorization'] ?? '';
    }

    $token = '';
    if (stripos($header, 'Bearer ') === 0) {
        $token = trim(substr($header, 7));
    } elseif (isset($_SERVER['HTTP_X_BACKUP_TOKEN'])) {
        $token = trim((string) $_SERVER['HTTP_X_BACKUP_TOKEN']);
    }

    if (!hash_equals(API_TOKEN, $token)) {
        json_response(['success' => false, 'message' => 'Unauthorized'], 401);
    }
}

function db(): PDO
{
    static $pdo = null;
    if ($pdo instanceof PDO) {
        return $pdo;
    }

    $dsn = 'mysql:host=' . DB_HOST . ';dbname=' . DB_NAME . ';charset=utf8mb4';
    $pdo = new PDO($dsn, DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    ]);
    return $pdo;
}

function request_json(): array
{
    $raw = file_get_contents('php://input') ?: '';
    $data = json_decode($raw, true);
    if (!is_array($data)) {
        json_response(['success' => false, 'message' => 'Invalid JSON body'], 400);
    }
    return $data;
}

function require_string(array $data, string $key): string
{
    $value = $data[$key] ?? null;
    if (!is_string($value) || trim($value) === '') {
        json_response(['success' => false, 'message' => "Missing field: {$key}"], 400);
    }
    return trim($value);
}

function require_int(array $data, string $key): int
{
    $value = $data[$key] ?? null;
    if (!is_int($value) && !is_float($value) && !ctype_digit((string) $value)) {
        json_response(['success' => false, 'message' => "Missing field: {$key}"], 400);
    }
    return (int) $value;
}

function cleanup_old_backups(PDO $pdo, string $deviceId): void
{
    $sql = "
        DELETE FROM muhasebecim_backups
        WHERE device_id = :delete_device_id
        AND id NOT IN (
            SELECT id FROM (
                SELECT id
                FROM muhasebecim_backups
                WHERE device_id = :keep_device_id
                ORDER BY id DESC
                LIMIT 10
            ) keep_rows
        )
    ";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        ':delete_device_id' => $deviceId,
        ':keep_device_id' => $deviceId,
    ]);
}
