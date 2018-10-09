package edu.baylor.ecs.cfgg.processor.service;

import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CouplingCohesionProcessorService extends ProcessorService {

    protected class MethodStats {
        public String className;
        public String methodName;
        public Set<String> coupledClasses;
        public Set<String> cohesiveMethods;
        //public Map<Integer, Integer> callsPerLevel;

        public void addMethod(String className, String methodName) {
            if (className.equals(this.className)) {
                this.cohesiveMethods.add(methodName);
            } else {
                this.coupledClasses.add(className);
            }
        }

        public String getFullName() {
            return this.className + "::" + this.methodName;
        }
    }

    @Override
    public String generateSourceCode() throws IOException, URISyntaxException {

        this.json = this.evaluatorRepository.getGraphInJsonFormat();

        List<String> splitJson = this.splitJSONObjects();

        for (String json : splitJson) {
            processJson(json);
        }

        return null;

    }

    @Override
    protected String processJson(String json) throws IOException {

        Map<String, List<List<String>>> map = this.populateParentsChildren(json);

        return null;

    }
}
