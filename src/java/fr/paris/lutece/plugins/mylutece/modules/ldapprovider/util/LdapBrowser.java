/*
 * Copyright (c) 2002-2019, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.mylutece.modules.ldapprovider.util;

import fr.paris.lutece.plugins.mylutece.modules.ldapprovider.business.LdapUser;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ldap.LdapUtil;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * LDAP browser : to retrieve information from LDAP repository
 */
public class LdapBrowser
{
    // ldap
    private static final String PROPERTY_INITIAL_CONTEXT_PROVIDER = "mylutece-ldapprovider.ldap.initialContextProvider";
    private static final String PROPERTY_PROVIDER_URL = "mylutece-ldapprovider.ldap.connectionUrl";
    private static final String PROPERTY_BIND_DN = "mylutece-ldapprovider.ldap.connectionName";
    private static final String PROPERTY_BIND_PASSWORD = "mylutece-ldapprovider.ldap.connectionPassword";
    private static final String PROPERTY_USER_DN_SEARCH_BASE = "mylutece-ldapprovider.ldap.userBase";
    private static final String PROPERTY_USER_DN_SEARCH_FILTER_BY_CRITERIA_SN = "mylutece-ldapprovider.ldap.userSearch.criteria.sn";
    private static final String PROPERTY_USER_DN_SEARCH_FILTER_BY_CRITERIA_GIVENNAME = "mylutece-ldapprovider.ldap.userSearch.criteria.givenname";
    private static final String PROPERTY_USER_DN_SEARCH_FILTER_BY_CRITERIA_MAIL = "mylutece-ldapprovider.ldap.userSearch.criteria.mail";
    private static final String PROPERTY_USER_SUBTREE = "mylutece-ldapprovider.ldap.userSubtree";
    private static final String PROPERTY_DN_ATTRIBUTE_GUID = "mylutece-ldapprovider.ldap.dn.attributeName.ldapGuid";
    private static final String PROPERTY_DN_ATTRIBUTE_LAST_NAME = "mylutece-ldapprovider.ldap.dn.attributeName.lastName";
    private static final String PROPERTY_DN_ATTRIBUTE_GIVEN_NAME = "mylutece-ldapprovider.ldap.dn.attributeName.givenName";
    private static final String PROPERTY_DN_ATTRIBUTE_EMAIL = "mylutece-ldapprovider.ldap.dn.attributeName.email";
    private static final String PROPERTY_DN_ATTRIBUTE_LOGIN = "mylutece-ldapprovider.ldap.dn.attributeName.login";

    private static final String ATTRIBUTE_LOGIN = AppPropertiesService.getProperty( PROPERTY_DN_ATTRIBUTE_LOGIN );
    private static final String ATTRIBUTE_GUID = AppPropertiesService.getProperty( PROPERTY_DN_ATTRIBUTE_GUID );
    private static final String ATTRIBUTE_LAST_NAME = AppPropertiesService.getProperty( PROPERTY_DN_ATTRIBUTE_LAST_NAME );
    private static final String ATTRIBUTE_GIVEN_NAME = AppPropertiesService.getProperty( PROPERTY_DN_ATTRIBUTE_GIVEN_NAME );
    private static final String ATTRIBUTE_EMAIL = AppPropertiesService.getProperty( PROPERTY_DN_ATTRIBUTE_EMAIL );
    /**
     * Search controls for the user entry search
     */
    private SearchControls _scUserSearchControls;
    /**
     * Context opened on the LDAP server
     */
    private DirContext _context;

    /**
     *
     */
    public LdapBrowser( )
    {
    }


    /**
     * Returns a list of users corresponding to the given parameters. An empty parameter is remplaced by the wildcard (*)
     *
     * @param strParameterLastName
     * @param strParameterFirstName
     * @param strParameterEmail
     * @return the LdapUser list
     */
    public Collection<LdapUser> getUserList( String strParameterLastName )
    {
        ArrayList<LdapUser> userList = new ArrayList<LdapUser>( );
        SearchResult sr = null;
        Object [ ] messageFormatParam = new Object [ 3];
        String [ ] messageFormatFilter = new String [ 3];
        start( );
        messageFormatParam [0] = checkSyntax( strParameterLastName );
        messageFormatParam [1] = checkSyntax( "" );
        messageFormatParam [2] = checkSyntax( "" );
        messageFormatFilter [0] = getUserDnSearchFilterByCriteriaSn( );
        messageFormatFilter [1] = getUserDnSearchFilterByCriteriaGivenname( );
        messageFormatFilter [2] = getUserDnSearchFilterByCriteriaMail( );
        String strUserSearchFilter = buildRequest( messageFormatFilter, messageFormatParam );
        try
        {
            NamingEnumeration<?> userResults = LdapUtil.searchUsers( _context, strUserSearchFilter, getUserDnSearchBase( ), "", _scUserSearchControls );
            while ( ( userResults != null ) && userResults.hasMore( ) )
            {
                sr = (SearchResult) userResults.next( );
                Attributes attributes = sr.getAttributes( );

                String strLdapLogin = "";
                if ( attributes.get( ATTRIBUTE_LOGIN ) != null )
                {
                    strLdapLogin = attributes.get( ATTRIBUTE_LOGIN ).get( ).toString( );
                }

                String strLdapId = "";
                if ( attributes.get( ATTRIBUTE_GUID ) != null )
                {
                    strLdapId = attributes.get( ATTRIBUTE_GUID ).get( ).toString( );
                }
                String strLastName = "";
                if ( attributes.get( ATTRIBUTE_LAST_NAME ) != null )
                {
                    strLastName = attributes.get( ATTRIBUTE_LAST_NAME ).get( ).toString( );
                }
                String strFirstName = "";
                if ( attributes.get( ATTRIBUTE_GIVEN_NAME ) != null )
                {
                    strFirstName = attributes.get( ATTRIBUTE_GIVEN_NAME ).get( ).toString( );
                }
                String strEmail = "";
                if ( attributes.get( ATTRIBUTE_EMAIL ) != null )
                {
                    strEmail = attributes.get( ATTRIBUTE_EMAIL ).get( ).toString( );
                }
                LdapUser user = new LdapUser( );
                user.setLdapGuid( strLdapId );
                user.setLastName( strLastName );
                user.setFirstName( strFirstName );
                user.setEmail( strEmail );
                user.setLdapLogin( strLdapLogin );
                userList.add( user );
            }
            return userList;
        }
        catch( CommunicationException e )
        {
            AppLogService.error( "Error while searching for users '" + "' with search filter : " + getDebugInfo( strUserSearchFilter ), e );
            return null;
        }
        catch( NamingException e )
        {
            AppLogService.error( "Error while searching for users ", e );
            return null;
        }
        finally
        {
            close( );
        }
    }

    /**
     * Replace the null string or empty string by the wilcard
     * 
     * @param in
     * @return
     */
    private String checkSyntax( String in )
    {
        return ( ( ( in == null ) || ( in.equals( "" ) ) ) ? "*" : in );
    }

    /**
     * Return info for debugging
     * 
     * @param strUserSearchFilter
     * @return
     */
    private String getDebugInfo( String strUserSearchFilter )
    {
        StringBuffer sb = new StringBuffer( );
        sb.append( "userBase : " );
        sb.append( getUserDnSearchBase( ) );
        sb.append( "\nuserSearch : " );
        sb.append( strUserSearchFilter );
        return sb.toString( );
    }

    /**
     * Get the initial context provider from the properties
     * 
     * @return
     */
    private String getInitialContextProvider( )
    {
        return AppPropertiesService.getProperty( PROPERTY_INITIAL_CONTEXT_PROVIDER );
    }

    /**
     * Get the provider url from the properties
     * 
     * @return
     */
    private String getProviderUrl( )
    {
        return AppPropertiesService.getProperty( PROPERTY_PROVIDER_URL );
    }

    /**
     * Get the base user dn from the properties
     * 
     * @return
     */
    private String getUserDnSearchBase( )
    {
        return AppPropertiesService.getProperty( PROPERTY_USER_DN_SEARCH_BASE );
    }

    /**
     * Get the filter for search by sn
     * 
     * @return
     */
    private String getUserDnSearchFilterByCriteriaSn( )
    {
        return AppPropertiesService.getProperty( PROPERTY_USER_DN_SEARCH_FILTER_BY_CRITERIA_SN );
    }

    /**
     * Get the filter for search by givenname
     * 
     * @return
     */
    private String getUserDnSearchFilterByCriteriaGivenname( )
    {
        return AppPropertiesService.getProperty( PROPERTY_USER_DN_SEARCH_FILTER_BY_CRITERIA_GIVENNAME );
    }

    /**
     * Get the filter for search by mail
     * 
     * @return
     */
    private String getUserDnSearchFilterByCriteriaMail( )
    {
        return AppPropertiesService.getProperty( PROPERTY_USER_DN_SEARCH_FILTER_BY_CRITERIA_MAIL );
    }

    /**
     * Get the user dn search scope
     * 
     * @return
     */
    private int getUserDnSearchScope( )
    {
        String strSearchScope = AppPropertiesService.getProperty( PROPERTY_USER_SUBTREE );
        if ( strSearchScope.equalsIgnoreCase( "true" ) )
        {
            return SearchControls.SUBTREE_SCOPE;
        }
        return SearchControls.ONELEVEL_SCOPE;
    }

    /**
     * get the bind dn
     * 
     * @return
     */
    private String getBindDn( )
    {
        return AppPropertiesService.getProperty( PROPERTY_BIND_DN );
    }

    /**
     * Get the bing password
     * 
     * @return
     */
    private String getBindPassword( )
    {
        return AppPropertiesService.getProperty( PROPERTY_BIND_PASSWORD );
    }

    /**
     * build request for search by sn, givenname and mail
     * 
     * @return
     */
    public String buildRequest( String [ ] messageFormatFilter, Object [ ] messageFormatParam )
    {
        String strUserSearchFilter = "(&";
        for ( int i = 0; i < messageFormatParam.length; i++ )
        {
            if ( messageFormatParam [i].equals( "*" ) == false )
            {
                strUserSearchFilter += MessageFormat.format( messageFormatFilter [i], messageFormatParam );
            }
        }
        strUserSearchFilter += ")";
        return strUserSearchFilter;
    }

    /**
     * Open the directory context used for authentication and authorization.
     * 
     * @throws NamingException
     */
    private void open( ) throws NamingException
    {
        if ( _context != null )
        {
            close( );
        }
        AppLogService.info( "Connecting to URL " + getProviderUrl( ) );
        _context = LdapUtil.getContext( getInitialContextProvider( ), getProviderUrl( ), getBindDn( ), getBindPassword( ) );
        AppLogService.info( "Connected to URL " + getProviderUrl( ) );
    }

    /**
     * Close the specified directory context
     */
    private void close( )
    {
        if ( _context == null )
        {
            return;
        }
        try
        {
            AppLogService.info( "Closing directory context" );
            LdapUtil.freeContext( _context );
        }
        catch( NamingException e )
        {
            AppLogService.error( "Error while closing the directory context", e );
        }
        _context = null;
    }

    /**
     * Prepare for the beginning of active use of the public methods of this component.
     */
    private void start( )
    {
        try
        {
            open( );
            _scUserSearchControls = new SearchControls( );
            _scUserSearchControls.setSearchScope( getUserDnSearchScope( ) );
            _scUserSearchControls.setReturningObjFlag( true );
            _scUserSearchControls.setCountLimit( 0 );
        }
        catch( NamingException e )
        {
            throw new AppException( "Error while opening the directory context", e );
        }
    }
}
