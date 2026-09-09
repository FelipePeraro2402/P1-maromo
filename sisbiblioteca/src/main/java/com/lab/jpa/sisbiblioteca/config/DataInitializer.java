package com.lab.jpa.sisbiblioteca.config;

import com.lab.jpa.sisbiblioteca.model.Autor;
import com.lab.jpa.sisbiblioteca.model.Livro;
import com.lab.jpa.sisbiblioteca.repository.AutorRepository;
import com.lab.jpa.sisbiblioteca.repository.LivroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Scanner;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AutorRepository autorRepository;
    private final LivroRepository livroRepository;

    public DataInitializer(AutorRepository autorRepository,
                           LivroRepository livroRepository) {
        this.autorRepository = autorRepository;
        this.livroRepository = livroRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        var scanner = new Scanner(System.in);
        var continuar = true;

        System.out.println("==========================================");
        System.out.println(" SISTEMA DE GESTÃO DE BIBLIOTECA JPA ");
        System.out.println("==========================================");

        while (continuar) {

            System.out.println("\nMENU DE OPÇÕES:");
            System.out.println("1 - Cadastrar Autor");
            System.out.println("2 - Listar Autores");
            System.out.println("3 - Cadastrar Livro");
            System.out.println("4 - Listar Livros");
            System.out.println("5 - Buscar Livro por Titulo");
            System.out.println("6 - Listar Livros por Autor");
            System.out.println("7 - Alterar Dados do Livro");
            System.out.println("8 - Excluir Autor");
            System.out.println("9 - Excluir Livro");
            System.out.println("0 - Sair");

            System.out.print("Escolha uma opção: ");
            var opcao = scanner.nextLine();

            continuar = switch (opcao) {

                case "1" -> {
                    cadastrarAutor(scanner);
                    yield true;
                }

                case "2" -> {
                    listarAutores();
                    yield true;
                }

                case "3" -> {
                    cadastrarLivro(scanner);
                    yield true;
                }

                case "4" -> {
                    listarLivros();
                    yield true;
                }

                case "5" -> {
                    buscarLivroPorTitulo(scanner);
                    yield true;
                }

                case "6" -> {
                    listarLivrosPorAutor(scanner);
                    yield true;
                }

                case "7" -> {
                    atualizarLivro(scanner);
                    yield true;
                }

                case "8" -> {
                    excluirAutor(scanner);
                    yield true;
                }

                case "9" -> {
                    excluirLivro(scanner);
                    yield true;
                }

                case "0" -> {
                    System.out.println("Encerrando aplicação...");
                    yield false;
                }

                default -> {
                    System.out.println("Opção inválida! Tente novamente.");
                    yield true;
                }
            };
        }

        System.out.println("Aplicação finalizada.");
    }

    private void cadastrarAutor(Scanner scanner) {

        System.out.print("Digite o nome do autor: ");
        var nome = scanner.nextLine();

        if (nome.isBlank()) {
            System.out.println("Nome inválido!");
            return;
        }

        var autor = new Autor(nome);
        autorRepository.save(autor);

        System.out.println(">>> Autor '" + autor.getNome() + "' cadastrado com ID: " + autor.getId());
    }

    private void listarAutores() {

        var autores = autorRepository.findAll();

        if (autores.isEmpty()) {
            System.out.println("Nenhum autor cadastrado.");
            return;
        }

        System.out.println("\n--- LISTA DE AUTORES ---");

        autores.forEach(a ->
                System.out.printf("ID: %d | Nome: %s%n", a.getId(), a.getNome()));

        System.out.println("------------------------");
    }

    private void cadastrarLivro(Scanner scanner) {

        listarAutores();

        System.out.print("Informe o ID do autor do livro: ");
        var idStr = scanner.nextLine();

        try {

            var autorId = Long.parseLong(idStr);
            Optional<Autor> autorOpt = autorRepository.findById(autorId);

            if (autorOpt.isEmpty()) {
                System.out.println("Autor não encontrado com o ID informado!");
                return;
            }

            System.out.print("Digite o título do livro: ");
            var titulo = scanner.nextLine();

            System.out.print("Digite o ano de publicação: ");
            var ano = Integer.parseInt(scanner.nextLine());

            var livro = new Livro(titulo, ano, autorOpt.get());
            livroRepository.save(livro);

            System.out.println(">>> Livro '" + livro.getTitulo() + "' cadastrado com sucesso!");

        } catch (NumberFormatException e) {
            System.out.println("Valor numérico inválido informado.");
        }
    }

    private void listarLivros() {

        var livros = livroRepository.findAll();

        if (livros.isEmpty()) {
            System.out.println("Nenhum livro cadastrado.");
            return;
        }

        System.out.println("\n--- LISTA DE LIVROS ---");

        livros.forEach(livro ->
                System.out.printf(
                        "ID: %d | Título: %s | Ano: %d | Autor: %s%n",
                        livro.getId(),
                        livro.getTitulo(),
                        livro.getAnoPublicacao(),
                        livro.getAutor().getNome()
                ));

        System.out.println("-----------------------");
    }

    private void buscarLivroPorTitulo(Scanner scanner){

        System.out.println("Digite o titulo:");
        var titulo = scanner.nextLine();

        var livros = livroRepository.findByTituloContainingIgnoreCase(titulo);

        if(livros.isEmpty()){  //isEmpty verifica se uma String ou uma estrutura de dados está vazia
            System.out.println("Livro nao encontrado");
            return;
        }
        livros.stream()
                .sorted((livro1, livro2) -> livro1.getTitulo().compareToIgnoreCase(livro2.getTitulo()))
                .forEach(livro -> System.out.printf("ID: %d | %s | %d | Autor: %s%n",
                                livro.getId(),
                                livro.getTitulo(),
                                livro.getAnoPublicacao(),
                                livro.getAutor().getNome()
                ));
    }

    private void listarLivrosPorAutor(Scanner scanner){

        listarAutores();

        System.out.println("Digite o ID do Autor: ");
        var id = Long.parseLong(scanner.nextLine());

        var autorOpt = autorRepository.findById(id);

        autorOpt.ifPresentOrElse(autor -> {

                    System.out.println("\nLivros de " + autor.getNome() + ":");

                    var livros = livroRepository.findByAutorId(id);

                    if (livros.isEmpty()) {
                        System.out.println("Este autor não possui livros.");
                        return;
                    }

                    livros.stream()
                            .sorted((a, b) -> a.getAnoPublicacao().compareTo(b.getAnoPublicacao()))
                            .forEach(l -> System.out.println(l.getTitulo() + " - " + l.getAnoPublicacao()
                            ));
                },

                () -> System.out.println("Autor não encontrado.")
        );
    }

    private void atualizarLivro(Scanner scanner){

        listarLivros();

        System.out.println("Digite o ID do Livro: ");
        var id = Long.parseLong(scanner.nextLine());

        var livroOpt = livroRepository.findById(id);

        livroOpt.ifPresentOrElse(livro -> {

                    System.out.println("Digite o Titulo Novo: ");
                    var titulo = scanner.nextLine();

                    System.out.println("Digite o Ano Novo: ");
                    var ano = Integer.parseInt(scanner.nextLine());

                    livro.setTitulo(titulo);
                    livro.setAnoPublicacao(ano);

                    livroRepository.save(livro);
                    System.out.println("Livro Atualizado!!");
        },
                () -> System.out.println("Livro Nao Encontrado")
        );
    }

    private void excluirAutor(Scanner scanner){

        listarAutores();

        System.out.println("Digite o ID do Autor que vai ser Excluido: ");
        var id = Long.parseLong(scanner.nextLine());

        var autorOpt = autorRepository.findById(id);

        autorOpt.ifPresentOrElse(autor -> {
            var livros = livroRepository.findByAutorId(id);

            if(!livros.isEmpty()){
                System.out.println("Nao e possivel remover este autor");
                System.out.println("Existem" + livros.size() + "livro(s) vinculado(s)");
                return;
            }

            autorRepository.delete(autor);
            System.out.println("Autor excluido com sucesso!!");
        },
                () -> System.out.println("Autor nao encontrado")
        );
    }

    private void excluirLivro(Scanner scanner){

        listarLivros();

        System.out.println("Digite o ID do Livro que vai ser Excluido: ");
        var id = Long.parseLong(scanner.nextLine());

        var livroOpt = livroRepository.findById(id);

        livroOpt.ifPresentOrElse(livro -> {

            livroRepository.delete(livro);
            System.out.println("Livro excluido com sucesso!!");
        },
                () -> System.out.println("Livro nao encontrado")
        );
    }
}