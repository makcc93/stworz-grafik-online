package online.stworzgrafik.StworzGrafik.draft.controller;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class DemandDraftController {
    private final DemandDraftService demandDraftService;

    @GetMapping("/:storeId/drafts/:draftId")
    public ResponseEntity<?> findDraft()
        //pomysl o tym jak powinien wygladac endpoint i skad ma sie wziac storeId
    //czy to nie powinno brac z sesji, a jesli logouje sie np dyrektor to wybiera on sklep z listy
    //czy storeId powinno byc zapisane w endpoint czy nie, czy bedzie zaczytane z zalgoowanego uzytkownika
    //czy bedzie wybrane z listy
}
