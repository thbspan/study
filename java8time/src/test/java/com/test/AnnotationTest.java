package com.test;

import com.test.annotation.Person;
import com.test.annotation.Persons;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnnotationTest {

    @Test
    public void testNew() {
        Annotation[] annotations = A.class.getAnnotations();
        assertEquals(annotations.length, 1);
        assertTrue(annotations[0] instanceof Persons);
        Persons persons = (Persons) annotations[0];
        for (Person person : persons.value()) {
            System.out.println(person.role());
        }

        // 或者
        Persons p = A.class.getAnnotation(Persons.class);
        if (p != null) {
            for (Person person : p.value()) {
                System.out.println(person.role());
            }
        }
    }
}

@Person(role = "ceo")
@Person(role = "husband")
@Person(role = "father")
@Person(role = "son")
@Person(role = "star")
class A {

}

