package cx.catapult.animals.web;


import cx.catapult.animals.domain.Cat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class CatsControllerIT {
    @LocalServerPort
    private int port;

    private URL base;

    private Cat cat = new Cat("Tom", "Bob cat");

    @Autowired
    private TestRestTemplate template;

    @BeforeEach
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/api/1/cats");
    }

    @Test
    public void createShouldWork() throws Exception {
        ResponseEntity<Cat> response = template.postForEntity(base.toString(), cat, Cat.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotEmpty();
        assertThat(response.getBody().getName()).isEqualTo(cat.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(cat.getDescription());
        assertThat(response.getBody().getGroup()).isEqualTo(cat.getGroup());
    }

    @Test
    public void allShouldWork() throws Exception {
        Collection items = template.getForObject(base.toString(), Collection.class);
        assertThat(items.size()).isGreaterThanOrEqualTo(7);
    }

    @Test
    public void allFilterShouldWork() throws Exception {
        create("FilteredCat");
        Collection filteredItems = template.getForObject(base.toString() + "?name=Filtered&description=Cat", Collection.class);
        assertThat(filteredItems.size()).isEqualTo(1);
    }

    @Test
    public void getShouldWork() throws Exception {
        Cat created = create("Test 1");
        ResponseEntity<String> response = template.getForEntity(base.toString() + "/" + created.getId(), String.class);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    public void deleteShouldWork() throws Exception {
        Cat deleted = create("Test Delete");
        ResponseEntity<String> response = template.getForEntity(base.toString() + "/" + deleted.getId(), String.class);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    public void updateShouldWork() throws Exception {
        Cat updated = create("Test Update");
        ResponseEntity<Cat> response = template.exchange(base.toString() + "/" + updated.getId(), HttpMethod.PUT, new HttpEntity<>(updated), Cat.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isNotEmpty();
        assertThat(response.getBody().getName()).isEqualTo(updated.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(updated.getDescription());
        assertThat(response.getBody().getGroup()).isEqualTo(updated.getGroup());
    }

    Cat create(String name) {
        Cat created = template.postForObject(base.toString(), new Cat(name, name), Cat.class);
        assertThat(created.getId()).isNotEmpty();
        assertThat(created.getName()).isEqualTo(name);
        return created;
    }
}
