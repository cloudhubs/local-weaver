package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.SeerSecurityNode;
import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerMsContext;
import edu.baylor.ecs.seer.common.context.SeerRequestContext;
import edu.baylor.ecs.seer.common.context.SeerSecurityContext;
import edu.baylor.ecs.seer.common.security.*;
import javassist.CtClass;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The SeerMsEntityContextService service constructs a
 * {@link edu.baylor.ecs.seer.common.context.SeerSecurityContext} from an initial list of
 * {@link javassist.CtClass} and a {@link SeerRequestContext}.
 *
 * @author  Jan Svacina
 * @version 1.0
 * @since   0.3.0
 */
@Service
public class SeerMsSecurityContextService {

    /**
     * This method returns a {@link SeerSecurityContext} populated from the global
     * {@link List} of {@link CtClass} objects and a {@link SeerRequestContext}.
     *
     * @param ctClasses the global {@link List} of {@link CtClass} objects
     * @param req the {@link SeerRequestContext} containing the path to the project to analyze
     *
     * @return a {@link SeerContext} populated with {@link SeerMsContext} objects
     */
    SeerSecurityContext getMsSeerSecurityContext(List<CtClass> ctClasses, SeerRequestContext req) {

        String roleHierarchy = req.getSecurityAnalyzerInterface();
        SeerSecurityNode root = createRoleTree(roleHierarchy);

        SeerSecurityContext securityContext = new SeerSecurityContext(roleHierarchy, root);

        SecurityFilterContext securityFilterContext =
                new SecurityFilterContext(new SecurityFilterGeneralAnnotationStrategy());

        /* Security method contains: name, roles and children */
        Set<SecurityRootMethod> rootMethods = new HashSet<>();

        /* ! getSecurityMethods indeed updates the set, despite the fact it retrieves nothing  */
        for ( CtClass ctClass : ctClasses ) {
            securityFilterContext.doFilter(ctClass, rootMethods);
        }
        securityContext.setSecurityRoots(rootMethods);

        reduceMethodRoles(securityContext);

        Map<String, SecurityMethod> map = buildMap(securityContext);

        List<SecurityMethod> allSecurityMethods = new ArrayList<>();
        for(Map.Entry entry : map.entrySet()){
            allSecurityMethods.add((SecurityMethod) entry.getValue());
        }

        List<SecurityMethod> violatingMethods = allSecurityMethods
                                            .stream()
                                            .filter(x -> x.getMethodRoles().size() > 1)
                                            .collect(Collectors.toList());

        Set<SeerSecurityConstraintViolation> violations = findViolations(securityContext, violatingMethods);
        securityContext.setSecurityViolations(violations);
        return securityContext;
    }

    private SeerSecurityNode createRoleTree(String roleDef) {
        String[] lines = roleDef.split("\n");
        if (lines[0].contains("->")) {
            System.out.println("ERROR! Line 0 should not be an edge!");
            return null;
        }

        SeerSecurityNode roleTree = new SeerSecurityNode(lines[0].replaceAll(" ", ""));

        for ( int i = 1; i < lines.length; i++ ) {
            String line = lines[i].replaceAll(" ", "");
            String[] split = line.split("->");
            if (split.length != 2) {
                System.out.println("ERROR! Bad input line on line " + i + "!");
                return null;
            }
            if (split[0].endsWith("<")) {
                String first = split[0].substring(0, split[0].length() - 1);
                if (!roleTree.insert(split[1], first)) {
                    System.out.println("ERROR! Bad input on line " + i + "!\n" +
                            "Left hand side of edge must appear earlier as right hand side!");
                    return null;
                }
                if (!roleTree.insert(first, split[1])) {
                    System.out.println("ERROR! Bad input on line " + i + "!\n" +
                            "Left hand side of edge must appear earlier as right hand side!");
                    return null;
                }
            } else {
                if (!roleTree.insert(split[1], split[0])) {
                    System.out.println("ERROR! Bad input on line " + i + "!\n" +
                            "Left hand side of edge must appear earlier as right hand side!");
                    return null;
                }
            }
        }

        return roleTree;
    }

    private void reduceMethodRoles(SeerSecurityContext context){
        for(SecurityRootMethod method : context.getSecurityRoots()){
            if(method.getMethodRoles().size() > 0){
                List<SecurityRole> roles = new ArrayList<>(method.getMethodRoles());

                int maxDepth = context.getRoot().depth(roles.get(0).getFormattedRoleName());
                SecurityRole minPermission = roles.get(0);

                for(int i = 1; i < roles.size(); i++){
                    int depth = context.getRoot().depth(roles.get(i).getFormattedRoleName());
                    if(depth > maxDepth){
                        minPermission = roles.get(i);
                        maxDepth = depth;
                    }
                }

                Set<SecurityRole> reducedRoles = new HashSet<>();
                reducedRoles.add(minPermission);

                method.setMethodRoles(reducedRoles);

            }
        }
    }

    private Map<String, SecurityMethod> buildMap(SeerSecurityContext context){

        Map<String, SecurityMethod> allSecurityMethods = new HashMap<>();
        Queue<SecurityMethod> queue = new LinkedList<>();

        for(SecurityRootMethod method : context.getSecurityRoots()){
            SecurityMethod securityMethod = new SecurityMethod(method.getMethodName(), method.getChildMethods(), method.getMethodRoles());
            allSecurityMethods.put(method.getMethodName(), securityMethod);
            queue.add(securityMethod);
        }

        do{
            SecurityMethod method = queue.remove();
            for(SecurityMethod m : method.getChildMethods()){
                SecurityMethod child = allSecurityMethods.getOrDefault(m.getMethodName(), new SecurityMethod(m.getMethodName()));
                child.getMethodRoles().addAll(method.getMethodRoles());
                allSecurityMethods.put(child.getMethodName(), child);
            }

            queue.addAll(method.getChildMethods());

        } while(!queue.isEmpty());


        return allSecurityMethods;
    }

    private Set<SeerSecurityConstraintViolation> findViolations(SeerSecurityContext context, List<SecurityMethod> violatingMethods){
        Set<SeerSecurityConstraintViolation> violations = new HashSet<>();

        for(SecurityMethod violatingMethod : violatingMethods){
            List<SecurityRole> roles = new ArrayList<>(violatingMethod.getMethodRoles());
            SecurityRole r1 = roles.get(0);
            SecurityRole r2 = roles.get(1);

            int depth1 = context.getRoot().depth(r1.getFormattedRoleName());
            int depth2 = context.getRoot().depth(r2.getFormattedRoleName());

            if(depth1 == -1 || depth2 == -1){
                violations.add(new InvalidSecurityRoleViolation(violatingMethod));
            } else if(depth2 > depth1){
                SeerSecurityNode n1 = context.getRoot().search(r1.getFormattedRoleName());
                boolean hierarchy = n1.childContains(r2.getFormattedRoleName());
                if(hierarchy){
                    violations.add(new HierarchyConstraintViolation(violatingMethod));
                } else {
                    violations.add(new UnrelatedAccessConstraintViolation(violatingMethod));
                }
            } else if(depth2 < depth1){
                SeerSecurityNode n2 = context.getRoot().search(r2.getFormattedRoleName());
                boolean hierarchy = n2.childContains(r1.getFormattedRoleName());
                if(hierarchy){
                    violations.add(new HierarchyConstraintViolation(violatingMethod));
                } else {
                    violations.add(new UnrelatedAccessConstraintViolation(violatingMethod));
                }
            } else {
                violations.add(new UnrelatedAccessConstraintViolation(violatingMethod));
            }
        }

        return violations;
    }
}
