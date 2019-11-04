import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface test {
    static test getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, test.class);
    }
}
