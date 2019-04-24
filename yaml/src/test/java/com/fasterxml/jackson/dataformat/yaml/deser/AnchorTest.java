package com.fasterxml.jackson.dataformat.yaml.deser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.ModuleTestBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;

public class AnchorTest extends ModuleTestBase
{//TODO alias of null
    static class ManyTypesObject
    {
        public int intField;
        public long longField;
        public float floatField;
        public double doubleField;
        public BigInteger bigIntegerField;
        public boolean booleanField;
        public String stringField;
        public ManyTypesObject singleChild;
        public List<ManyTypesObject> childrenList;
        public Map<String, ManyTypesObject> childrenMap;

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ManyTypesObject that = (ManyTypesObject) o;

            if (intField != that.intField) return false;
            if (longField != that.longField) return false;
            if (Float.compare(that.floatField, floatField) != 0) return false;
            if (Double.compare(that.doubleField, doubleField) != 0) return false;
            if (booleanField != that.booleanField) return false;
            if (bigIntegerField != null ? !bigIntegerField.equals(that.bigIntegerField) : that.bigIntegerField != null)
                return false;
            if (stringField != null ? !stringField.equals(that.stringField) : that.stringField != null) return false;
            if (singleChild != null ? !singleChild.equals(that.singleChild) : that.singleChild != null) return false;
            if (childrenList != null ? !childrenList.equals(that.childrenList) : that.childrenList != null)
                return false;
            return childrenMap != null ? childrenMap.equals(that.childrenMap) : that.childrenMap == null;
        }

        @Override
        public String toString()
        {
            return "ManyTypesObject{" +
                    "intField=" + intField +
                    ", longField=" + longField +
                    ", floatField=" + floatField +
                    ", doubleField=" + doubleField +
                    ", bigIntegerField=" + bigIntegerField +
                    ", booleanField=" + booleanField +
                    ", stringField='" + stringField + '\'' +
                    ", singleChild=" + singleChild +
                    ", childrenList=" + childrenList +
                    ", childrenMap=" + childrenMap +
                    '}';
        }
    }

    private final ObjectMapper MAPPER = newObjectMapper();

    public void testSingleAnchor() throws Exception
    {
        final String yaml = loadYaml("singleAnchor.yaml");
        ManyTypesObject[] target = MAPPER.readValue(yaml, ManyTypesObject[].class);
        assertEquals(target[0], target[1]);
    }

    public void testReferencesToAnyType() throws Exception
    {
        final String yaml = loadYaml("referencesToAnyType.yaml");
        ManyTypesObject[] target = MAPPER.readValue(yaml, ManyTypesObject[].class);
        assertEquals(target[0], target[1]);
    }

    public void testSeveralAliasesOfOneObject() throws Exception
    {
        final String yaml = loadYaml("severalAliasesOfOneObject.yaml");
        ManyTypesObject[] target = MAPPER.readValue(yaml, ManyTypesObject[].class);
        assertEquals(target[0], target[1]);
        assertEquals(target[0], target[2]);
        assertEquals(target[0], target[3]);
    }

    public void testReferenceChainInArray() throws Exception
    {
        final String yaml = loadYaml("referenceChainInArray.yaml");
        ManyTypesObject[] target = MAPPER.readValue(yaml, ManyTypesObject[].class);
        assertEquals(target[0], target[1].singleChild);

        assertEquals(target[0], target[2].childrenList.get(0));

        assertEquals(target[0], target[2].childrenList.get(0));
        assertEquals(target[1], target[2].childrenList.get(1));

        assertEquals(target[0], target[3].childrenMap.get("reference0"));
        assertEquals(target[1], target[3].childrenMap.get("reference1"));
        assertEquals(target[2], target[3].childrenMap.get("reference2"));
    }

    public void testNestedAnchors() throws Exception
    {
        final String yaml = loadYaml("nestedAnchors.yaml");
        ManyTypesObject[] target = MAPPER.readValue(yaml, ManyTypesObject[].class);

        ManyTypesObject childOfChildOfRoot = new ManyTypesObject();
        childOfChildOfRoot.stringField = "child of child of root";

        ManyTypesObject childOfRoot = new ManyTypesObject();
        childOfRoot.stringField = "child of root";
        childOfRoot.singleChild = childOfChildOfRoot;

        ManyTypesObject root = new ManyTypesObject();
        root.stringField = "root";
        root.singleChild = childOfRoot;
        root.childrenList = Arrays.asList(childOfRoot, childOfChildOfRoot);
        root.childrenMap = new HashMap<>();
        root.childrenMap.put("childOfRoot", childOfRoot);
        root.childrenMap.put("childOfChildOfRoot", childOfChildOfRoot);

        assertEquals(root, target[0]);
        assertEquals(childOfChildOfRoot, target[1]);
        assertEquals(childOfRoot, target[2]);
        assertEquals(root, target[3]);
    }

    public void testOverriddenAnchors() throws Exception
    {
        final String yaml = loadYaml("overriddenAnchors.yaml");
        ManyTypesObject[] target = MAPPER.readValue(yaml, ManyTypesObject[].class);
        assertEquals(target[1].stringField, "overridden object");
    }

    private String loadYaml(String resourceFile) throws IOException
    {
        String resourcePath = getClass().getPackage().getName().replace('.', '/') + '/' + resourceFile;
        StringBuilder result = new StringBuilder();
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        Objects.requireNonNull(resourceAsStream, resourcePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append('\n');
            }
        }
        return result.toString();
    }
}
