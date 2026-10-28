INSERT INTO support_sectors (name, acronym) VALUES
    ('Coordenação de Tecnologia da Informação e Comunicação', 'COTIC'),
    ('Coordenação de Manutenção', 'COMAN');

INSERT INTO categories (name, description, support_sector_id) VALUES
    ('Rede e Internet', 'Problemas de conectividade, Wi-Fi e rede cabeada.', 1),
    ('Computadores', 'Problemas em computadores e periféricos.', 1),
    ('Elétrica', 'Problemas em instalações e equipamentos elétricos.', 2),
    ('Hidráulica', 'Problemas em instalações hidráulicas.', 2);

INSERT INTO users (name, email, registration, type, support_sector_id) VALUES
    ('Ana Solicitante', 'ana@ifrn.edu.br', '20260001', 'SOLICITANTE', NULL),
    ('Bruno Solicitante', 'bruno@ifrn.edu.br', '20260002', 'SOLICITANTE', NULL),
    ('Carlos Técnico', 'carlos@ifrn.edu.br', '20260003', 'TECNICO', 1),
    ('Diana Técnica', 'diana@ifrn.edu.br', '20260004', 'TECNICO', 2);
