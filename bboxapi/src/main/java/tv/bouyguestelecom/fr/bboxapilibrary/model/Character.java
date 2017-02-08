package tv.bouyguestelecom.fr.bboxapilibrary.model;

import java.io.Serializable;

public class Character implements Serializable, Comparable<Character>{
    public static final String TAG_FIRSTNAME = "firstName";
    public static final String TAG_LASTNAME = "lastName";
    public static final String TAG_ROLE = "role";
    public static final String TAG_FUNCTION = "function";
    public static final String TAG_RANK = "rank";

    private String firstName;
    private String lastName;
    private String role;
    private String function;
    private int rank;

    public Character(String firstName, String lastName, String role, String function, int rank) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.function = function;
        this.rank = rank;
    }


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRole() {
        return role;
    }

    public String getFunction() {
        return function;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return "Character{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role='" + role + '\'' +
                ", function='" + function + '\'' +
                ", rank=" + rank +
                '}';
    }

    @Override
    public int compareTo(Character another) {
        return lastName.compareTo(another.getLastName());
    }
}
