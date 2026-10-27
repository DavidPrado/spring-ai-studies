-- V1_0_11__create_user_profile_pictures_table.sql
CREATE TABLE IF NOT EXISTS user_profile_pictures (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    picture_data BYTEA NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size INTEGER NOT NULL,
    file_name VARCHAR(255),
    thumbnail_data BYTEA,  -- Thumbnail pequeno para listagens
    thumbnail_size INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_profile_pictures_user_id ON user_profile_pictures(user_id);
CREATE INDEX IF NOT EXISTS idx_profile_pictures_updated_at ON user_profile_pictures(updated_at);

-- Comentários
COMMENT ON TABLE user_profile_pictures IS 'Tabela separada para fotos de perfil - melhora performance da tabela users';
COMMENT ON COLUMN user_profile_pictures.picture_data IS 'Foto original em BYTEA';
COMMENT ON COLUMN user_profile_pictures.thumbnail_data IS 'Thumbnail para listagens rápidas';
COMMENT ON COLUMN user_profile_pictures.content_type IS 'Tipo MIME da imagem';

-- Trigger para atualizar updated_at
CREATE OR REPLACE FUNCTION update_profile_picture_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_profile_picture_timestamp
    BEFORE UPDATE ON user_profile_pictures
    FOR EACH ROW
    EXECUTE FUNCTION update_profile_picture_updated_at();