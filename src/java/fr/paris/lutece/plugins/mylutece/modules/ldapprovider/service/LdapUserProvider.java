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
package fr.paris.lutece.plugins.mylutece.modules.ldapprovider.service;

import java.util.Collection;
import java.util.List;
import fr.paris.lutece.plugins.mylutece.modules.ldapprovider.business.LdapUser;
import fr.paris.lutece.plugins.mylutece.modules.ldapprovider.util.LdapBrowser;
import fr.paris.lutece.plugins.mylutece.modules.users.business.LocalUser;
import fr.paris.lutece.plugins.mylutece.modules.users.service.IUserInfosProvider;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceList;

import java.util.ArrayList;

/**
 * Service to get a Person from a LDAP
 */
public class LdapUserProvider implements IUserInfosProvider
{
    @Override
    public List<LocalUser> findUsers( String strUserLastName, String strParameterGivenName, String strParameterCriteriaMail, ReferenceList listProviderAttribute )
    {
        LdapBrowser ldap = new LdapBrowser( );
        Collection<LdapUser> userList = null;
        List<LocalUser> localUserList = new ArrayList<>( );
        try
        {
            userList = ldap.getUserList( strUserLastName, strParameterGivenName, strParameterCriteriaMail, listProviderAttribute );
        }
        catch( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        for ( LdapUser ldapUser : userList )
        {
            LocalUser user = new LocalUser( );
            user.setEmail( ldapUser.getEmail( ) );
            user.setGivenName( ldapUser.getFirstName( ) );
            user.setLastName( ldapUser.getLastName( ) );
            user.setLogin( ldapUser.getLdapLogin( ) );
            user.setProviderUserId( ldapUser.getLdapGuid( ) );
            user.setAttributes( ldapUser.getAttributes( ) );
            localUserList.add( user );
        }
        return localUserList;
    }

    @Override
    public List<String> getAllAttributes( )
    {
        LdapBrowser ldap = new LdapBrowser( );
        List<String> attributeList = null;
        try
        {
            attributeList = ldap.getAllAttributes( );
        }
        catch( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        return attributeList;
    }
}
