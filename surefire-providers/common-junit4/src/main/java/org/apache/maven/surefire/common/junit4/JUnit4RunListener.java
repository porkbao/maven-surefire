package org.apache.maven.surefire.common.junit4;

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

import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JUnit4RunListener
    extends org.junit.runner.notification.RunListener
{
    private static final Pattern PARENS = Pattern.compile( "^" + "[^\\(\\)]+" //non-parens
                                                               + "\\((" // then an open-paren (start matching a group)
                                                               + "[^\\\\(\\\\)]+" //non-parens
                                                               + ")\\)" + "$" ); // then a close-paren (end group match)

    protected final RunListener reporter;

    /**
     * This flag is set after a failure has occurred so that a <code>testSucceeded</code> event is not fired.
     * This is necessary because JUnit4 always fires a <code>testRunFinished</code> event-- even if there was a failure.
     */
    private ThreadLocal failureFlag = new InheritableThreadLocal();

    /**
     * Constructor.
     *
     * @param reporter the reporter to log testing events to
     */
    public JUnit4RunListener( RunListener reporter )
    {
        this.reporter = reporter;
    }

    // Testrun methods are not invoked when using the runner

    /**
     * Called when a specific test has been skipped (for whatever reason).
     *
     * @see org.junit.runner.notification.RunListener#testIgnored(org.junit.runner.Description)
     */
    public void testIgnored( Description description )
        throws Exception
    {
        reporter.testSkipped( createReportEntry( description ) );
    }

    /**
     * Called when a specific test has started.
     *
     * @see org.junit.runner.notification.RunListener#testStarted(org.junit.runner.Description)
     */
    public void testStarted( Description description )
        throws Exception
    {
        reporter.testStarting( createReportEntry( description ) );
        failureFlag.remove( );
    }

    /**
     * Called when a specific test has failed.
     *
     * @see org.junit.runner.notification.RunListener#testFailure(org.junit.runner.notification.Failure)
     */
    @SuppressWarnings( { "ThrowableResultOfMethodCallIgnored" } )
    public void testFailure( Failure failure )
        throws Exception
    {
        ReportEntry report =
            new SimpleReportEntry( extractClassName( failure.getDescription() ), failure.getTestHeader(),
                                   new JUnit4StackTraceWriter( failure ) );

        if ( failure.getException() instanceof AssertionError )
        {
            this.reporter.testFailed( report );
        }
        else
        {
            this.reporter.testError( report );
        }
        failureFlag.set( Boolean.TRUE );
    }

    public void testAssumptionFailure( Failure failure )
    {
        this.reporter.testAssumptionFailure( createReportEntry( failure.getDescription() ) );
        failureFlag.set( Boolean.TRUE );
    }


    /**
     * Called after a specific test has finished.
     *
     * @see org.junit.runner.notification.RunListener#testFinished(org.junit.runner.Description)
     */
    public void testFinished( Description description )
        throws Exception
    {
        Boolean failure = (Boolean) failureFlag.get();
        if ( failure == null )
        {
            reporter.testSucceeded( createReportEntry( description ) );
        }
    }

    private SimpleReportEntry createReportEntry( Description description )
    {
        return new SimpleReportEntry( extractClassName( description ), description.getDisplayName() );
    }


    protected String extractClassName( Description description )
    {
        String displayName = description.getDisplayName();
        Matcher m = PARENS.matcher( displayName );
        if ( !m.find() )
        {
            return displayName;
        }
        return m.group( 1 );
    }
}
