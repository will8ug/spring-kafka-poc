package io.will.poc.kafka.test.suite;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("io.will.poc.kafka")
@IncludeClassNamePatterns(value = {"io.will.poc.kafka.Dlt\\w*IT"})
public class DltCasesSuite {
}
