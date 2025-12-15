package online.stworzgrafik.StworzGrafik.temporaryUser.roleStrategy;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserRole;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserRoleStrategyFactory {
    private final Map<String, UserRoleStrategy> strategies;

    public UserRoleStrategy getStrategy(UserRole userRole){
        UserRoleStrategy strategy = strategies.get(userRole.name());

        if (strategy == null){
            throw new IllegalStateException("Cannot find strategy to user role " + userRole.name());
        }

        return strategy;
    }
}
