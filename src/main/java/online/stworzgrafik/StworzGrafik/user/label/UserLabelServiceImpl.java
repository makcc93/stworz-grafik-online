package online.stworzgrafik.StworzGrafik.user.label;

import online.stworzgrafik.StworzGrafik.user.AppUser;
import org.springframework.stereotype.Service;

@Service
public class UserLabelServiceImpl implements UserLabelService{

    @Override
    public String buildLabel(AppUser user) {
        if (user == null) return "Użytkownik";

        return switch (user.getRole()) {
            case ADMIN -> "Administrator";
            case DIRECTOR -> buildDirectorLabel(user);
            case STORE_MANAGER -> {
                String name = user.getStore() != null ? user.getStore().getName() : null;
                yield (name != null && !name.isBlank()) ? "Kierownik Sklepu " + name : "Kierownik Sklepu";
            }
            default -> "Użytkownik";
        };
    }

    private String buildDirectorLabel(AppUser user) {
        if (user.getDirectorScope() == null) return "Dyrektor";
        return switch (user.getDirectorScope()) {
            case NETWORK -> "Dyrektor Sieci";
            case REGION -> {
                String name = user.getRegion() != null ? user.getRegion().getName() : null;
                yield (name != null && !name.isBlank()) ? "Dyrektor Regionu " + name : "Dyrektor Regionu";
            }
            case BRANCH -> {
                String name = user.getBranch() != null ? user.getBranch().getName() : null;
                yield (name != null && !name.isBlank()) ? "Dyrektor Oddziału " + name : "Dyrektor Oddziału";
            }
        };
    }
}
