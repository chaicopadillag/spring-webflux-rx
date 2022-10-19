package dev.team.springbootreactor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import dev.team.springbootreactor.models.Comment;
import dev.team.springbootreactor.models.User;
import dev.team.springbootreactor.models.UserComment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// exampleMapAndFilter();
		// exampleFlatMap();
		// exampleCollectList();
		// userCommentsFlatMap();
		// userCommentsZipWith();
		// userCommentsZipWithFormeTwo();
		// exampleZipWithRange();
		// exampleInterval();
		// exampleDelayElement();
		// exampleDelayInfinito();
		// exampleIntervalDesdeCreate();
		// exampleContraPresion();
	}

	public void exampleContraPresion() {

		Flux.range(1, 10)
				.log()
				// .limitRate(5)
				.subscribe(new Subscriber<Integer>() {

					private Subscription s;

					private Integer limite = 5;
					private Integer consumido = 0;

					@Override
					public void onSubscribe(Subscription s) {
						this.s = s;
						s.request(limite);
					}

					@Override
					public void onNext(Integer t) {
						log.info(t.toString());
						consumido++;
						if (consumido == limite) {
							consumido = 0;
							s.request(limite);
						}
					}

					@Override
					public void onError(Throwable t) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onComplete() {
						// TODO Auto-generated method stub

					}
				});
	}

	public void exampleIntervalDesdeCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++contador);
					if (contador == 10) {
						timer.cancel();
						emitter.complete();
					}

					if (contador == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
					}

				}
			}, 1000, 1000);
		}).subscribe(next -> log.info(next.toString()), error -> log.error(error.getMessage()),
				() -> log.info("Hemos terminado"));
	}

	private void exampleDelayInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(i -> {
					if (i >= 5) {
						return Flux.error(new InterruptedException("Solo hasta 5!"));
					}
					return Flux.just(i);
				}).map(i -> "Hola " + i)
				.retry(2)
				.subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

		latch.await();
	}

	private void exampleDelayElement() {
		Flux<Integer> range = Flux.range(0, 12).delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		range.subscribe();
		// range.blockLast();

	}

	private void exampleInterval() {
		Flux<Integer> range = Flux.range(0, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		range.zipWith(retraso, (ra, re) -> ra).doOnNext(i -> log.info(i.toString())).subscribe();

	}

	private void exampleZipWithRange() {
		Flux<Integer> range = Flux.range(0, 4);

		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9)
				.map(i -> i * 2)
				.zipWith(range, (uno, dos) -> String.format("Primer Flux: %d, segundo flux: %d", uno, dos))
				.subscribe(text -> log.info(text));

	}

	public void exampleMapAndFilter() throws Exception {

		List<String> listNames = new ArrayList<>();
		listNames.add("Pedro Rios");
		listNames.add("Jose Fulano");
		listNames.add("Pedro Mengano");
		listNames.add("Maria Pansa");
		listNames.add("Anita Perez");
		listNames.add("Jose Maria");

		// Flux<User> names = Flux.just("Jose Fulano", "Pedro Mengano", "Maria Pansa",
		// "Anita Perez", "Jose Maria").
		Flux<User> names = Flux.fromIterable(listNames)
				.map(name -> new User(name.split(" ")[0].toLowerCase(), name.split(" ")[1].toLowerCase()))
				.filter(user -> user.getName().equalsIgnoreCase("Jose"))
				// .filter(user -> user.getName().equals("MARIA"))
				.doOnNext(user -> {

					if (user == null) {
						throw new RuntimeException("User is Empty");
					}

					System.out.println(user.toString());

				});

		names.subscribe(user -> log.info(user.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				System.out.println("complete exec observable");

			}

		});

	}

	public void exampleFlatMap() throws Exception {

		List<String> listNames = new ArrayList<>();

		listNames.add("Pedro Rios");
		listNames.add("Jose Fulano");
		listNames.add("Pedro Mengano");
		listNames.add("Maria Pansa");
		listNames.add("Anita Perez");
		listNames.add("Jose Maria");

		Flux.fromIterable(listNames)
				.map(name -> new User(name.split(" ")[0].toLowerCase(), name.split(" ")[1].toLowerCase()))
				.flatMap(user -> user.getName().equalsIgnoreCase("Jose") ? Mono.just(user) : Mono.empty())
				.subscribe(user -> log.info(user.toString()));

	}

	public void exampleListUserToString() throws Exception {

		List<User> listUsers = new ArrayList<>();

		listUsers.add(new User("Pedro", " Rios"));
		listUsers.add(new User("Jose", " Fulano"));
		listUsers.add(new User("Pedro", " Mengano"));
		listUsers.add(new User("Maria", " Pansa"));
		listUsers.add(new User("Anita", " Perez"));
		listUsers.add(new User("Jose", " Maria"));

		Flux.fromIterable(listUsers)
				.map(user -> user.getLastName().concat(" ").concat(user.getLastName()))
				.flatMap(name -> name.contains("Jose") ? Mono.just(name) : Mono.empty())
				.subscribe(name -> log.info(name));

	}

	public void exampleCollectList() throws Exception {

		List<User> listUsers = new ArrayList<>();

		listUsers.add(new User("Pedro", " Rios"));
		listUsers.add(new User("Jose", " Fulano"));
		listUsers.add(new User("Pedro", " Mengano"));
		listUsers.add(new User("Maria", " Pansa"));
		listUsers.add(new User("Anita", " Perez"));
		listUsers.add(new User("Jose", " Maria"));

		Flux.fromIterable(listUsers)
				.collectList()
				.subscribe(list -> {
					list.forEach(user -> log.info(list.toString()));
				});

	}

	public void userCommentsFlatMap() {
		Mono<User> user = Mono.fromCallable(() -> new User("Fulano", "Mengano"));

		Mono<Comment> comments = Mono.fromCallable(() -> {
			Comment comment = new Comment();
			comment.addComment("Hello world");
			comment.addComment("Form Course Udemy");
			comment.addComment("in Spring Boot");

			return comment;
		});

		user.flatMap(user_ -> comments.map(comment -> new UserComment(user_, comment)))
				.subscribe(userComments -> log.info(userComments.toString()));

	}

	public void userCommentsZipWith() {
		Mono<User> user = Mono.fromCallable(() -> new User("Fulano", "Mengano"));

		Mono<Comment> comments = Mono.fromCallable(() -> {
			Comment comment = new Comment();
			comment.addComment("Hello world");
			comment.addComment("Form Course Udemy");
			comment.addComment("in Spring Boot");

			return comment;
		});

		Mono<UserComment> userComments = user.zipWith(comments, (user_, comment_) -> new UserComment(user_, comment_));
		userComments.subscribe(userComments_ -> log.info(userComments_.toString()));

	}

	public void userCommentsZipWithFormeTwo() {
		Mono<User> user = Mono.fromCallable(() -> new User("Fulano", "Mengano"));

		Mono<Comment> comments = Mono.fromCallable(() -> {
			Comment comment = new Comment();
			comment.addComment("Hello world");
			comment.addComment("Form Course Udemy");
			comment.addComment("in Spring Boot");

			return comment;
		});

		Mono<UserComment> userComments = user.zipWith(comments).map(tupla -> {
			User u = tupla.getT1();
			Comment c = tupla.getT2();
			return new UserComment(u, c);
		});

		userComments.subscribe(userComments_ -> log.info(userComments_.toString()));

	}

}
