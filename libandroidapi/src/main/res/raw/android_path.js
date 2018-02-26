/*
 * Copyright (c) 2018 Altimit Community Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or imp
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

( function() {

    var _basenames = {};
    var _dirnames = {};
    var _joins = {};

    androidRequire.path = {

        basename: function( path, ext ) {
            if ( !_basenames[path] ) {
                _basenames[path] = {};
            }
            if ( !_basenames[path][ext] ) {
                if ( typeof path !== 'string' ) {
                    throw 'path is not a string';
                }
                if ( ext && typeof ext !== 'string' ) {
                    throw 'ext is not a string';
                }
                _basenames[path][ext] = {
                    value:__android_api_path.pathBasename( path, ext )
                }
            }
            return _basenames[path][ext].value;
        },

        dirname: function( path ) {
            if ( !_dirnames[path] ) {
                if ( typeof path !== 'string' ) {
                    throw 'path is not a string';
                }
                _dirnames[path] = {
                    value: __android_api_path.pathDirname( path )
                }
            }
            return _dirnames[path].value;
        },

        join: function() {
            if ( !_joins[arguments] ) {
                for ( var ii = 0; ii < arguments.length; ii++ ) {
                    if ( typeof arguments[ii] !== 'string' ) {
                        throw 'argument ' + ii + ' is not a string';
                    }
                }
                _joins[arguments] = {
                    value: __android_api_path.pathJoin( arguments.length === 1 ? [arguments[0]] : Array.apply( null, arguments ) )
                }
            }
            return _joins[arguments].value;
        },

    };

} )();