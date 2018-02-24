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

    /**
     * FileCache
     */
    function FileCache() {
        throw new Error( 'This is a static class' );
    }
    ( function() {

        FileCache.limit = 1024 * 1024; // ~1 MiB
        FileCache._items = {};

        FileCache.add = function( key, value ) {
            this._items[key] = {
                contents: value,
                touch: Date.now(),
                key: key
            };
            this._truncateCache();
        };

        FileCache.get = function( key ) {
            if ( this._items[key] ) {
                var item = this._items[key];
                item.touch = Date.now();
                return item.contents;
            }
            return null;
        };

        FileCache.remove = function( key ) {
            delete this._items[key];
        };

        FileCache._truncateCache = function(){
            var items = this._items;
            var sizeLeft = FileCache.limit;

            Object.keys( items ).map( function( key ) {
                return items[key];
            } ).sort( function( a, b ) {
                return b.touch - a.touch;
            } ).forEach( function( item ) {
                if ( sizeLeft > 0 ){
                    var contents = item.contents;
                    sizeLeft -= contents.length;
                } else {
                    delete items[item.key];
                }
            }.bind( this ) );
        };

    } )();

    var _exists = {};

    androidRequire.fs = {

        existsSync: function( path ) {
            if ( !_exists[path] ) {
                _exists[path] = {
                    value: __android_api_fs.fsExistsSync( path ) == 'true'
                }
            }
            return _exists[path].value;
        },

        mkdirSync: function( path, mode ) {
            return __android_api_fs.fsMkdirSync( path );
        },

        writeFileSync: function( file, data, options ) {
            _exists[file] = {
                value: true
            };

            FileCache.add( file, data );
            return __android_api_fs.fsWriteFileSync( file, data );
        },

        readFileSync: function( path, options ) {
            var contents = FileCache.get( path );
            if ( !contents ) {
                contents = __android_api_fs.fsReadFileSync( path );
                if ( !!contents ) {
                    FileCache.add( path, contents );
                }
            }
            return contents;
        },

        unlinkSync: function( path ) {
            _exists[path] = {
                value: false
            };

            FileCache.remove( path );
            return __android_api_fs.fsUnlinkSync( path );
        },

    };

} )();