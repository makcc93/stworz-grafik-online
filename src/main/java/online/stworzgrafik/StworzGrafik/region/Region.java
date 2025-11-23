package online.stworzgrafik.StworzGrafik.region;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.branch.Branch;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private boolean enable;

    @OneToMany(mappedBy = "region")
    private List<Branch> branches = new ArrayList<>();

    @PrePersist
    void onCreate(){
        this.enable = true;
    }

}
