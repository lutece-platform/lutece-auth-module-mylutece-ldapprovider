/*
 * Copyright (c) 2002-2020, City of Paris
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
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.ldap.LdapUtil;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.Attribute;
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
    private static final String PROPERTY_MAPPING_OBJECT_CLASS_LIST = "mylutece-ldapprovider.ldap.mapping.objectClassList";
    private static final String PROPERTY_MAPPING_ATTRIBUTE_LIST = "mylutece-ldapprovider.ldap.mapping.attributeNameList";
    private static final String PROPERTY_DN_ATTRIBUTE_GUID = "mylutece-ldapprovider.ldap.dn.attributeName.ldapGuid";
    private static final String PROPERTY_DN_ATTRIBUTE_LAST_NAME = "mylutece-ldapprovider.ldap.dn.attributeName.lastName";
    private static final String PROPERTY_DN_ATTRIBUTE_GIVEN_NAME = "mylutece-ldapprovider.ldap.dn.attributeName.givenName";
    private static final String PROPERTY_DN_ATTRIBUTE_EMAIL = "mylutece-ldapprovider.ldap.dn.attributeName.email";
    private static final String PROPERTY_DN_ATTRIBUTE_LOGIN = "mylutece-ldapprovider.ldap.dn.attributeName.login";
    private static final String PROPERTY_WILDCARD = "mylutece-ldapprovider.wildcard";
    private static final String PROPERTY_WILDCARD_MIN_LENGTH = "mylutece-ldapprovider.wildcard.miniumLength";

    private static final String ATTRIBUTE_LOGIN = AppPropertiesService.getProperty( PROPERTY_DN_ATTRIBUTE_LOGIN );
    private static final String ATTRIBUTE_GUID = AppPropertiesService.getProperty( PROPERTY_DN_ATTRIBUTE_GUID );
    private static final String ATTRIBUTE_LAST_NAME = AppPropertiesService.getProperty( PROPERTY_DN_ATTRIBUTE_LAST_NAME );
    private static final String ATTRIBUTE_GIVEN_NAME = AppPropertiesService.getProperty( PROPERTY_DN_ATTRIBUTE_GIVEN_NAME );
    private static final String ATTRIBUTE_EMAIL = AppPropertiesService.getProperty( PROPERTY_DN_ATTRIBUTE_EMAIL );
    private static final String ATTRIBUTE_WILCARD = AppPropertiesService.getProperty( PROPERTY_WILDCARD );
    private static final String ATTRIBUTE_WILCARD_MIN_LENGTH = AppPropertiesService.getProperty( PROPERTY_WILDCARD_MIN_LENGTH );
    private static final String ATTRIBUTE_MAPPING_OBJECT_CLASS_LIST = AppPropertiesService.getProperty( PROPERTY_MAPPING_OBJECT_CLASS_LIST );
    private static final String ATTRIBUTE_MAPPING_ATTRIBUTE_LIST = AppPropertiesService.getProperty( PROPERTY_MAPPING_ATTRIBUTE_LIST );

    /**
     * How many guids travel in one search filter
     */
    private static final int GUID_BATCH_SIZE = 50;

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
     * @param listProviderAttribute
     * @return the LdapUser list
     */
    public Collection<LdapUser> getUserList( String strParameterLastName, String strParameterGivenName, String strParameterCriteriaMail,
            ReferenceList listProviderAttribute )
    {
        ArrayList<LdapUser> userList = new ArrayList<LdapUser>( );
        SearchResult sr = null;
        if ( isEmptySearchRequest( strParameterLastName, strParameterGivenName, strParameterCriteriaMail, listProviderAttribute ) )
        {
            return userList;
        }
        Object [ ] messageFormatParam = new Object [ 3 + listProviderAttribute.size( )];
        String [ ] messageFormatFilter = new String [ 3 + listProviderAttribute.size( )];
        start( );
        messageFormatParam [0] = checkCriteriaParameterSyntax( strParameterLastName );
        messageFormatParam [1] = checkCriteriaParameterSyntax( strParameterGivenName );
        messageFormatParam [2] = checkCriteriaParameterSyntax( strParameterCriteriaMail );
        messageFormatFilter [0] = getUserDnSearchFilterByCriteriaSn( );
        messageFormatFilter [1] = getUserDnSearchFilterByCriteriaGivenname( );
        messageFormatFilter [2] = getUserDnSearchFilterByCriteriaMail( );
        int position = 3;
        for ( ReferenceItem providerAttribute : listProviderAttribute )
        {
            messageFormatParam [position] = checkCriteriaParameterSyntax( providerAttribute.getCode( ) );
            messageFormatFilter [position] = "(" + providerAttribute.getName( ) + "=" + "{" + position + "})";
            position++;
        }
        String strUserSearchFilter = buildRequest( messageFormatFilter, messageFormatParam );
        try
        {
            NamingEnumeration<?> userResults = LdapUtil.searchUsers( _context, strUserSearchFilter, getUserDnSearchBase( ), "", _scUserSearchControls );
            while ( ( userResults != null ) && userResults.hasMore( ) )
            {
                sr = (SearchResult) userResults.next( );
                Attributes attributes = sr.getAttributes( );
                LdapUser user = new LdapUser( );
                if ( attributes.get( ATTRIBUTE_GUID ) != null )
                {
                    user.setLdapGuid( (String) attributes.get( ATTRIBUTE_GUID ).get( ) );
                    user.setLastName( checkAttributeSyntax( attributes.get( ATTRIBUTE_LAST_NAME ) ) );
                    user.setFirstName( checkAttributeSyntax( attributes.get( ATTRIBUTE_GIVEN_NAME ) ) );
                    user.setEmail( checkAttributeSyntax( attributes.get( ATTRIBUTE_EMAIL ) ) );
                    user.setLdapLogin( checkAttributeSyntax( attributes.get( ATTRIBUTE_LOGIN ) ) );
                    user.setAttributes( getAttributeWithResultValue( attributes, listProviderAttribute ) );
                    userList.add( user );
                }
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
     * Returns a list of attributes valued from search result
     *
     * @return the attribute list
     * @throws NamingException
     */
    public ReferenceList getAttributeWithResultValue( Attributes attributes, ReferenceList listProviderAttribute ) throws NamingException
    {
        ReferenceList listUserProviderAttribute = new ReferenceList( );
        for ( ReferenceItem providerAttribute : listProviderAttribute )
        {
            ReferenceItem userProviderAttribute = new ReferenceItem( );
            userProviderAttribute.setName( providerAttribute.getName( ) );
            if ( attributes.get( providerAttribute.getName( ) ) != null )
            {
                userProviderAttribute.setCode( attributes.get( providerAttribute.getName( ) ).get( ).toString( ) );
            }
            else
            {
                userProviderAttribute.setCode( null );
            }
            listUserProviderAttribute.add( userProviderAttribute );
        }
        return listUserProviderAttribute;
    }

    /**
     * Reads the attributes the mapping declares from a search result, so a caller knows the
     * organisation a user belongs to without a second lookup.
     *
     * @param attributes
     *            the attributes returned by the directory
     * @return the declared attributes carrying a value, name and value of each item
     * @throws NamingException
     *             if a value cannot be read
     */
    private static ReferenceList readMappedAttributes( Attributes attributes ) throws NamingException
    {
        ReferenceList listAttributes = new ReferenceList( );

        if ( ATTRIBUTE_MAPPING_ATTRIBUTE_LIST == null || attributes == null )
        {
            return listAttributes;
        }

        for ( String strDeclared : ATTRIBUTE_MAPPING_ATTRIBUTE_LIST.split( "," ) )
        {
            String strAttribute = strDeclared.trim( );
            String strValue = strAttribute.isEmpty( ) ? null : readFirstValue( attributes.get( strAttribute ) );

            if ( strValue != null )
            {
                ReferenceItem item = new ReferenceItem( );
                item.setName( strAttribute );
                item.setCode( strValue );
                listAttributes.add( item );
            }
        }

        return listAttributes;
    }

    /**
     * Returns the users carrying the given guids, resolved in one directory round trip per batch
     * rather than one per guid: a bulk lookup would otherwise open as many connections as guids.
     *
     * @param listGuids
     *            the guids to resolve
     * @return the users found, in directory order, missing guids simply absent
     */
    public Collection<LdapUser> getUsersByGuids( List<String> listGuids )
    {
        List<LdapUser> userList = new ArrayList<>( );
        if ( listGuids == null || listGuids.isEmpty( ) )
        {
            return userList;
        }

        try
        {
            start( );
            for ( int nStart = 0; nStart < listGuids.size( ); nStart += GUID_BATCH_SIZE )
            {
                List<String> listBatch = listGuids.subList( nStart, Math.min( nStart + GUID_BATCH_SIZE, listGuids.size( ) ) );
                userList.addAll( searchUsersByGuidBatch( listBatch ) );
            }
        }
        catch( NamingException e )
        {
            AppLogService.error( "Error while searching for users by guid", e );
        }
        finally
        {
            close( );
        }

        return userList;
    }

    /**
     * Searches one batch of guids with a single filter, on the already opened context.
     *
     * @param listGuids
     *            the guids of the batch
     * @return the users found
     * @throws NamingException
     *             if the directory refuses the search
     */
    private List<LdapUser> searchUsersByGuidBatch( List<String> listGuids ) throws NamingException
    {
        List<LdapUser> userList = new ArrayList<>( );
        StringBuilder sbFilter = new StringBuilder( "(|" );
        for ( String strGuid : listGuids )
        {
            if ( strGuid != null && !strGuid.trim( ).isEmpty( ) )
            {
                sbFilter.append( "(" ).append( ATTRIBUTE_GUID ).append( "=" ).append( escapeFilterValue( strGuid.trim( ) ) ).append( ")" );
            }
        }
        sbFilter.append( ")" );

        if ( sbFilter.length( ) <= 3 )
        {
            return userList;
        }

        NamingEnumeration<?> userResults = LdapUtil.searchUsers( _context, sbFilter.toString( ), getUserDnSearchBase( ), "", _scUserSearchControls );
        while ( ( userResults != null ) && userResults.hasMore( ) )
        {
            SearchResult sr = (SearchResult) userResults.next( );
            LdapUser user = toLdapUser( sr.getAttributes( ) );
            if ( user != null )
            {
                userList.add( user );
            }
        }

        return userList;
    }

    /**
     * Builds a user from the attributes of a search result.
     *
     * @param attributes
     *            the attributes returned by the directory
     * @return the user, null when it carries no guid
     * @throws NamingException
     *             if an attribute cannot be read
     */
    private LdapUser toLdapUser( Attributes attributes ) throws NamingException
    {
        if ( attributes.get( ATTRIBUTE_GUID ) == null )
        {
            return null;
        }

        LdapUser user = new LdapUser( );
        user.setLdapGuid( (String) attributes.get( ATTRIBUTE_GUID ).get( ) );
        user.setLastName( checkAttributeSyntax( attributes.get( ATTRIBUTE_LAST_NAME ) ) );
        user.setFirstName( checkAttributeSyntax( attributes.get( ATTRIBUTE_GIVEN_NAME ) ) );
        user.setEmail( checkAttributeSyntax( attributes.get( ATTRIBUTE_EMAIL ) ) );
        user.setLdapLogin( checkAttributeSyntax( attributes.get( ATTRIBUTE_LOGIN ) ) );
        return user;
    }

    /**
     * Escapes the characters a search filter would otherwise read as syntax.
     *
     * @param strValue
     *            the raw value
     * @return the value, safe to embed in a filter
     */
    private static String escapeFilterValue( String strValue )
    {
        return strValue.replace( "\\", "\\5c" ).replace( "(", "\\28" ).replace( ")", "\\29" ).replace( "*", "\\2a" ).replace( "\u0000", "\\00" );
    }

    /**
     * Returns the distinct combinations of values the given attributes take across the user
     * subtree. Only those attributes are fetched, so the whole directory is walked without loading
     * full user entries. Knowing which values coexist is what lets a suggestion list narrow another.
     *
     * @param listAttributeNames
     *            the attributes to enumerate together
     * @return the distinct combinations, an attribute missing from an entry missing from its map
     */
    public List<Map<String, String>> getAttributeValues( List<String> listAttributeNames )
    {
        List<Map<String, String>> listCombinations = new ArrayList<>( );
        if ( listAttributeNames == null || listAttributeNames.isEmpty( ) )
        {
            return listCombinations;
        }

        List<String> listAttributes = new ArrayList<>( );
        StringBuilder sbFilter = new StringBuilder( "(|" );
        for ( String strAttributeName : listAttributeNames )
        {
            if ( strAttributeName != null && !strAttributeName.trim( ).isEmpty( ) )
            {
                String strAttribute = strAttributeName.trim( );
                listAttributes.add( strAttribute );
                sbFilter.append( "(" ).append( strAttribute ).append( "=*)" );
            }
        }
        sbFilter.append( ")" );

        if ( listAttributes.isEmpty( ) )
        {
            return listCombinations;
        }

        Set<Map<String, String>> setCombinations = new LinkedHashSet<>( );
        try
        {
            start( );
            SearchControls searchControls = new SearchControls( );
            searchControls.setSearchScope( getUserDnSearchScope( ) );
            searchControls.setCountLimit( 0 );
            searchControls.setReturningAttributes( listAttributes.toArray( new String [ 0 ] ) );

            NamingEnumeration<?> results = LdapUtil.searchUsers( _context, sbFilter.toString( ), getUserDnSearchBase( ), "", searchControls );
            while ( ( results != null ) && results.hasMore( ) )
            {
                SearchResult sr = (SearchResult) results.next( );
                Map<String, String> mapCombination = new LinkedHashMap<>( );
                for ( String strAttribute : listAttributes )
                {
                    String strValue = readFirstValue( sr.getAttributes( ).get( strAttribute ) );
                    if ( strValue != null )
                    {
                        mapCombination.put( strAttribute, strValue );
                    }
                }
                if ( !mapCombination.isEmpty( ) )
                {
                    setCombinations.add( mapCombination );
                }
            }
        }
        catch( NamingException e )
        {
            AppLogService.error( "Error while enumerating the values of attributes {}", listAttributes, e );
        }
        finally
        {
            close( );
        }

        listCombinations.addAll( setCombinations );
        return listCombinations;
    }

    /**
     * Reads the first value of an attribute.
     *
     * @param attribute
     *            the attribute, may be null
     * @return the value, null when the attribute is absent or blank
     * @throws NamingException
     *             if the value cannot be read
     */
    private static String readFirstValue( Attribute attribute ) throws NamingException
    {
        if ( attribute == null || attribute.size( ) == 0 )
        {
            return null;
        }
        Object value = attribute.get( );
        if ( value == null || value.toString( ).trim( ).isEmpty( ) )
        {
            return null;
        }
        return value.toString( ).trim( );
    }

    /**
     * Returns a list of all attributes from ldap schema according to objectclasslist properties
     *
     * @return the attribute list
     */
    public List<String> getAllAttributes( )
    {
        List<String> attributesNames = new ArrayList<String>( );
        try
        {
            start( );
            DirContext schema = _context.getSchema( "" );
            List<String> classNamesList = new ArrayList<>();
            NamingEnumeration classDefinitionlistBindings = schema.listBindings("ClassDefinition");
            while (classDefinitionlistBindings.hasMore())
            {
                Binding classDefinitionBinding = (Binding) classDefinitionlistBindings.next();
                classNamesList.add(classDefinitionBinding.getName());
            }
            for(String strObjectClass : ATTRIBUTE_MAPPING_OBJECT_CLASS_LIST.split(",")) {
                if(classNamesList.contains(strObjectClass)) {
                    BasicAttributes basicAttributes = (BasicAttributes) schema.getAttributes( "ClassDefinition/" + strObjectClass );
                    NamingEnumeration<Attribute> basicAttributesList = basicAttributes.getAll( );
                    Attribute basicAttribute = null;
                    while ( ( basicAttributesList != null ) && basicAttributesList.hasMore( ) )
                    {
                        basicAttribute = basicAttributesList.next( );
                        NamingEnumeration<?> attributesList = basicAttribute.getAll( );
                        while ( ( attributesList != null ) && attributesList.hasMore( ) )
                        {
                            attributesNames.add( attributesList.next( ).toString( ) );
                        }
                    }
                }
            }
            for(String strAttributeName : ATTRIBUTE_MAPPING_ATTRIBUTE_LIST.split(",")) {
                attributesNames.add( strAttributeName );
            }
        }
        catch( NamingException e )
        {
            AppLogService.error( "Error while getting all attributes", e );
            return null;
        }
        finally
        {
            close( );
        } 
        return attributesNames.stream().distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Return a complient criterial parameter according to wildcard properties
     * 
     * @param strCriteriaParameterValue
     * @return a complient value 
     */
    private String checkCriteriaParameterSyntax( String strCriteriaParameterValue )
    {
        if ( strCriteriaParameterValue == null || strCriteriaParameterValue.isEmpty( ) )
        {
            strCriteriaParameterValue = ATTRIBUTE_WILCARD;
        }
        if ( ATTRIBUTE_WILCARD != null && ATTRIBUTE_WILCARD_MIN_LENGTH != null )
        {
            if ( strCriteriaParameterValue.length( ) >= Integer.parseInt( ATTRIBUTE_WILCARD_MIN_LENGTH ) )
            {
                strCriteriaParameterValue = strCriteriaParameterValue + ATTRIBUTE_WILCARD;
            }
        }
        return strCriteriaParameterValue;
    }

    /**
     * Returns true if all criterias are empty
     *
     * @param strParameterLastName
     * @param strParameterFirstName
     * @param strParameterEmail
     * @param listProviderAttribute
     * @return true if all criterias are empty
     */
    private boolean isEmptySearchRequest( String strParameterLastName, String strParameterGivenName, String strParameterCriteriaMail,
            ReferenceList listProviderAttribute )
    {
        if ( !strParameterLastName.isEmpty( ) || !strParameterGivenName.isEmpty( ) || !strParameterCriteriaMail.isEmpty( ) )
        {
            return false;
        }
        for ( ReferenceItem referenceItem : listProviderAttribute )
        {
            if ( !referenceItem.getCode( ).isEmpty( ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Return a complient criterial parameter
     * 
     * @param in
     * @return
     * @throws NamingException
     */
    private String checkAttributeSyntax( Attribute attribute ) throws NamingException
    {
        String strAttributeValue = "";
        if ( attribute != null )
        {
            strAttributeValue = (String) attribute.get( );
        }
        return strAttributeValue.toString( );
    }

    public LdapUser getUserByGuid( String strGuid )
    {
        if ( strGuid == null || strGuid.trim( ).isEmpty( ) )
        {
            return null;
        }

        start( );
        try
        {
            String strUserSearchFilter = "(" + ATTRIBUTE_GUID + "=" + strGuid + ")";
            NamingEnumeration<?> userResults = LdapUtil.searchUsers( _context, strUserSearchFilter, getUserDnSearchBase( ), "", _scUserSearchControls );

            if ( userResults != null && userResults.hasMore( ) )
            {
                SearchResult sr = (SearchResult) userResults.next( );
                Attributes attributes = sr.getAttributes( );

                if ( attributes.get( ATTRIBUTE_GUID ) != null )
                {
                    LdapUser user = new LdapUser( );
                    user.setLdapGuid( (String) attributes.get( ATTRIBUTE_GUID ).get( ) );
                    user.setLastName( checkAttributeSyntax( attributes.get( ATTRIBUTE_LAST_NAME ) ) );
                    user.setFirstName( checkAttributeSyntax( attributes.get( ATTRIBUTE_GIVEN_NAME ) ) );
                    user.setEmail( checkAttributeSyntax( attributes.get( ATTRIBUTE_EMAIL ) ) );
                    user.setLdapLogin( checkAttributeSyntax( attributes.get( ATTRIBUTE_LOGIN ) ) );
                    user.setAttributes( readMappedAttributes( attributes ) );
                    return user;
                }
            }
            return null;
        }
        catch( NamingException e )
        {
            AppLogService.error( "Error while searching for user by GUID: " + strGuid, e );
            return null;
        }
        finally
        {
            close( );
        }
    }

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
