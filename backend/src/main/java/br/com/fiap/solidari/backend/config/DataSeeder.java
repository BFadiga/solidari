package br.com.fiap.solidari.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.fiap.solidari.backend.model.Papel;
import br.com.fiap.solidari.backend.model.Parceiro;
import br.com.fiap.solidari.backend.model.Usuario;
import br.com.fiap.solidari.backend.repository.ParceiroRepository;
import br.com.fiap.solidari.backend.repository.UsuarioRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ParceiroRepository parceiroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            ParceiroRepository parceiroRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.parceiroRepository = parceiroRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedParceiros();
        seedAdmin();
    }

    private void seedParceiros() {
        if (parceiroRepository.count() > 0) {
            return;
        }

        Parceiro greenLeaf = new Parceiro();
        greenLeaf.setNome("Green Leaf Cafe");
        greenLeaf.setDescricao("Refeições sustentáveis e cafés especiais");
        greenLeaf.setCategoria("Meio Ambiente");
        greenLeaf.setBadge("15% CASHBACK");
        greenLeaf.setImagemUrl("https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&q=80");
        greenLeaf.setDestaque(true);

        Parceiro vitalityGym = new Parceiro();
        vitalityGym.setNome("Vitality Gym");
        vitalityGym.setDescricao("Saúde e Bem-estar");
        vitalityGym.setCategoria("Saúde");
        vitalityGym.setBadge("10% OFF");
        vitalityGym.setImagemUrl("https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400&q=80");
        vitalityGym.setDestaque(false);

        Parceiro ecoThreads = new Parceiro();
        ecoThreads.setNome("Eco Threads");
        ecoThreads.setDescricao("Moda Consciente");
        ecoThreads.setCategoria("Comunidade");
        ecoThreads.setBadge("SOLIDARITY+");
        ecoThreads.setImagemUrl("https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400&q=80");
        ecoThreads.setDestaque(false);

        parceiroRepository.saveAll(java.util.List.of(greenLeaf, vitalityGym, ecoThreads));
    }

    private void seedAdmin() {
        if (usuarioRepository.existsByEmail("admin@solidari.com")) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome("Admin Solidari");
        admin.setEmail("admin@solidari.com");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setPapel(Papel.ADMIN);
        admin.setSaldo(0.0);

        usuarioRepository.save(admin);
    }
}
