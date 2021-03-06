package org.apache.maven.surefire.its;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.FileUtils;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.surefire.its.misc.HelperAssertions;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Test a directory with an umlaut
 *
 * @author <a href="mailto:dfabulich@apache.org">Dan Fabulich</a>
 */
public class UmlautDirIT
    extends AbstractSurefireIntegrationTestClass
{
    File testDir;

    public void testUmlaut()
        throws Exception
    {
        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        this.executeGoal( verifier, "test" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        HelperAssertions.assertTestSuiteResults( 1, 0, 0, 0, testDir );
    }

    public void testUmlautIsolatedClassLoader()
        throws Exception
    {
        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        List<String> goals = this.getInitialGoals();
        goals.add( "test" );
        goals.add( "-DuseSystemClassLoader=false" );
        executeGoals( verifier, goals );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        HelperAssertions.assertTestSuiteResults( 1, 0, 0, 0, testDir );
    }

    public void setUp()
        throws IOException
    {
        String tempDirPath = System.getProperty( "maven.test.tmpdir", System.getProperty( "java.io.tmpdir" ) );
        File tempDir = new File( tempDirPath );
        File targetDir = new File( "target" ).getAbsoluteFile();
        if ( targetDir.exists() && targetDir.isDirectory() )
        {
            tempDir = targetDir;
        }
        testDir = new File( tempDir, "/junit-pathWith\u00DCmlaut" );
        FileUtils.deleteDirectory( testDir );
        testDir = ResourceExtractor.extractResourcePath( getClass(), "/junit-pathWithUmlaut", testDir, true );
    }
}
