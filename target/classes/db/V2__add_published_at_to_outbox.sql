ALTER TABLE outbox
ADD COLUMN IF NOT EXISTS published_at timestamptz;

-- Si ya tienes filas publicadas, inicialízalas:
UPDATE outbox SET published_at = now() WHERE published = true AND published_at IS NULL;
