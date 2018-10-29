package edu.baylor.ecs.seer.lweaver.mock;

import javax.persistence.*;

@Entity
public class MockEntity {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;

        @Column(length = 25)
        private String firstName;

        @Column(name = "last-name", length = 20)
        private String lastName;

        protected MockEntity() {}

        public MockEntity(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public String toString() {
            return String.format(
                    "Customer[id=%d, firstName='%s', lastName='%s']",
                    id, firstName, lastName);
        }


}
