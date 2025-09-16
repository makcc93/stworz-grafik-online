package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private boolean isEnable;

    @OneToMany(mappedBy = "branch")
    private List<Store> stores = new ArrayList<>();

    @PrePersist
    void onCreate(){
        isEnable = true;
    }
}
