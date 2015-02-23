package org.sample;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by hmorimoto on 2/20/15.
 * Using JMH (Open JDK provided microbench framework)
 * Bench mark with Java 8 ParallelStream vs. Single stream implementation.
 * Do, create 1M people
 * Filter by Age
 * Sort by Salary
 * Limit top X people
 * Map output to String type
 * Make List
 * <p>
 * Warm up x1
 * Iterate x3 times.
 */
public class StreamSample {

    private static final int MAX_PEOPLE = 1000000;

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void singleProcess() {
        System.out.println("******* RUNNING SINGLE PROCESS MODE ******* ");

        List<Person> people = randomPeople(MAX_PEOPLE);
        // Limit age  20 - 60, top 5 people in terms of salary.
        List resultList = people.stream()
                .filter(p -> p.age >= 20 && p.age < 60)
                .sorted((p1, p2) -> (p2.salary <= p1.salary ? -1 : 1))
                        //.sorted( (p1, p2) -> p1.name.compareTo(p2.name))
                .limit(5)
                .map(p -> "Mr. " + p.name + " earns $" + p.salary)
                .collect(Collectors.toList());
        System.out.println("Result: " + resultList.toString());
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void multiProcess() {
        System.out.println("******* RUNNING MULTI PROCESS MODE ******* ");

        List<Person> people = randomPeople(MAX_PEOPLE);
        List resultList = people.parallelStream()
                .filter(p -> p.age >= 20 && p.age < 60)
                .sorted((p1, p2) -> (p2.salary <= p1.salary ? -1 : 1))
                .limit(5)
                .map(p -> "Mr. " + p.name + " earns $" + p.salary)
                .collect(Collectors.toList());
        System.out.println("Result: " + resultList.toString());
    }


    /**
     * *********************************************
     * *
     * Down below helper method.                  *
     * *
     * **********************************************
     */
    // Just create N number of people with randomly generated age, salary, and name.
    public static List<Person> randomPeople(int nPeople) {
        List<Person> people = new ArrayList<>(nPeople);
        for (int i = 0; i < nPeople; i++) {
            people.add(new Person(randomString(), randomAge(), randomSalary()));
        }
        return people;
    }

    private static SecureRandom random = new SecureRandom();

    private static String randomString() {
        return new BigInteger(130, random).toString(32);
    }

    // Age between 15 - 85
    private static int randomAge() {
        return (int) (Math.random() * 70 + 15);
    }

    private static double randomSalary() {
        return new BigDecimal(Math.random() * 10000).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static class Person {
        public Person(String name, int age, double salary) {
            this.name = name;
            this.age = age;
            this.salary = salary;
        }

        public final String name;
        public final int age;
        public final double salary;

        public String doSomething() {
            return "My Age*Salary is: " + age * salary;
        }
    }
}
