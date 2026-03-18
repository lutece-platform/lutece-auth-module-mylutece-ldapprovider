/*
 * Copyright (c) 2002-2025, City of Paris
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
package fr.paris.lutece.plugins.mylutece.modules.ldapprovider.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.paris.lutece.plugins.mylutece.modules.ldapprovider.business.LdapUser;
import fr.paris.lutece.plugins.mylutece.modules.ldapprovider.util.LdapBrowser;
import fr.paris.lutece.plugins.mylutece.service.search.IUserSearchProvider;
import fr.paris.lutece.plugins.mylutece.service.search.MyLuteceSearchUser;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * IUserSearchProvider implementation for LDAP authentication.
 */
@ApplicationScoped
@Named( "mylutece.myLuteceUserSearchProvider" )
public class LdapSearchProvider implements IUserSearchProvider
{
    /**
     * {@inheritDoc}
     */
    @Override
    public List<MyLuteceSearchUser> findUsers( String strParameterLastName, String strParameterGivenName, String strParameterCriteriaMail,
            ReferenceList listProviderAttribute )
    {
        LdapBrowser ldap = new LdapBrowser( );
        List<MyLuteceSearchUser> result = new ArrayList<>( );

        try
        {
            ReferenceList attributes = listProviderAttribute != null ? listProviderAttribute : new ReferenceList( );
            Collection<LdapUser> userList = ldap.getUserList( strParameterLastName, strParameterGivenName, strParameterCriteriaMail, attributes );

            if ( userList != null )
            {
                for ( LdapUser ldapUser : userList )
                {
                    result.add( convertToMyLuteceSearchUser( ldapUser ) );
                }
            }
        }
        catch( Exception e )
        {
            AppLogService.error( "Error while searching for users in LDAP", e );
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllAttributes( )
    {
        LdapBrowser ldap = new LdapBrowser( );
        try
        {
            List<String> attributes = ldap.getAllAttributes( );
            return attributes != null ? attributes : new ArrayList<>( );
        }
        catch( Exception e )
        {
            AppLogService.error( "Error while getting LDAP attributes", e );
            return new ArrayList<>( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MyLuteceSearchUser getUserById( String strUserId )
    {
        if ( strUserId == null || strUserId.trim( ).isEmpty( ) )
        {
            return null;
        }

        LdapBrowser ldap = new LdapBrowser( );
        try
        {
            LdapUser ldapUser = ldap.getUserByGuid( strUserId );
            if ( ldapUser != null )
            {
                return convertToMyLuteceSearchUser( ldapUser );
            }
        }
        catch( Exception e )
        {
            AppLogService.error( "Error while getting user by ID from LDAP: {}", strUserId, e );
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MyLuteceSearchUser> getUsersByIds( List<String> userIds )
    {
        List<MyLuteceSearchUser> result = new ArrayList<>( );

        if ( userIds == null || userIds.isEmpty( ) )
        {
            return result;
        }

        for ( String userId : userIds )
        {
            MyLuteceSearchUser user = getUserById( userId );
            if ( user != null )
            {
                result.add( user );
            }
        }

        return result;
    }

    /**
     * Converts a LdapUser to a MyLuteceSearchUser.
     *
     * @param ldapUser the LDAP user to convert
     * @return the converted MyLuteceSearchUser
     */
    private MyLuteceSearchUser convertToMyLuteceSearchUser( LdapUser ldapUser )
    {
        MyLuteceSearchUser user = new MyLuteceSearchUser( );
        user.setId( ldapUser.getUserId( ) );
        user.setLogin( ldapUser.getLdapLogin( ) );
        user.setGivenName( ldapUser.getFirstName( ) );
        user.setLastName( ldapUser.getLastName( ) );
        user.setEmail( ldapUser.getEmail( ) );
        user.setProviderUserId( ldapUser.getLdapGuid( ) );
        user.setAttributes( ldapUser.getAttributes( ) );
        return user;
    }
}
