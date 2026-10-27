CREATE TABLE tenant_metrics_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    total_users INT,
    storage_size_mb DECIMAL(10,2),
    cpu_usage_seconds DECIMAL(10,2),
    CONSTRAINT fk_metrics_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX idx_metrics_tenant_date ON tenant_metrics_history(tenant_id, recorded_at);