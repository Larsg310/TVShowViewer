package nl.larsgerrits.tvshows;

import bt.BtException;
import bt.data.StorageUnit;
import bt.data.file.FileSystemStorage;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.StringTokenizer;

public class TVShowFileSystemStorage extends FileSystemStorage
{
    private final Path rootDirectory;
    private final PathNormalizer pathNormalizer;
    private final String fileName;
    
    public TVShowFileSystemStorage(Path rootDirectory, String fileName)
    {
        super(rootDirectory);
        this.rootDirectory = rootDirectory;
        this.pathNormalizer = new PathNormalizer(rootDirectory.getFileSystem());
        this.fileName = fileName;
    }
    
    @Override
    public StorageUnit getUnit(Torrent torrent, TorrentFile torrentFile)
    {
        
        Path torrentDirectory;
        if (torrent.getFiles().size() == 1) torrentDirectory = rootDirectory;
        else
        {
            String normalizedName = pathNormalizer.normalize(torrent.getName());
            torrentDirectory = rootDirectory.resolve(normalizedName);
        }
        // String normalizedPath = pathNormalizer.normalize(torrentFile.getPathElements());
        return new TVShowFileSystemStorageUnit(torrentDirectory, fileName, torrentFile.getSize());
    }
    
    class PathNormalizer
    {
        private final String separator;
        
        public PathNormalizer(FileSystem fileSystem)
        {
            separator = fileSystem.getSeparator();
        }
        
        public String normalize(List<String> path)
        {
            if (path.isEmpty())
            {
                return "_";
            }
            else if (path.size() == 1)
            {
                return normalize(path.get(0));
            }
            else
            {
                StringBuilder buf = new StringBuilder();
                path.forEach(element -> {
                    buf.append(element);
                    buf.append(separator);
                });
                buf.delete(buf.length() - separator.length(), buf.length());
                return normalize(buf.toString());
            }
        }
        
        public String normalize(String path)
        {
            String normalized = path.trim();
            if (normalized.isEmpty())
            {
                return "_";
            }
            
            StringTokenizer tokenizer = new StringTokenizer(normalized, separator, true);
            StringBuilder buf = new StringBuilder(normalized.length());
            boolean first = true;
            while (tokenizer.hasMoreTokens())
            {
                String element = tokenizer.nextToken();
                if (separator.equals(element))
                {
                    if (first)
                    {
                        buf.append("_");
                    }
                    buf.append(separator);
                    // this will handle inner slash sequences, like ...a//b...
                    first = true;
                }
                else
                {
                    buf.append(normalizePathElement(element));
                    first = false;
                }
            }
            
            normalized = buf.toString();
            return replaceTrailingSlashes(normalized);
        }
        
        private String normalizePathElement(String pathElement)
        {
            // truncate leading and trailing whitespaces
            String normalized = pathElement.trim();
            if (normalized.isEmpty())
            {
                return "_";
            }
            
            // truncate trailing whitespaces and dots;
            // this will also eliminate '.' and '..' relative names
            char[] value = normalized.toCharArray();
            int to = value.length;
            while (to > 0 && (value[to - 1] == '.' || value[to - 1] == ' '))
            {
                to--;
            }
            if (to == 0)
            {
                normalized = "";
            }
            else if (to < value.length)
            {
                normalized = normalized.substring(0, to);
            }
            
            return normalized.isEmpty() ? "_" : normalized;
        }
        
        private String replaceTrailingSlashes(String path)
        {
            if (path.isEmpty())
            {
                return path;
            }
            
            int k = 0;
            while (path.endsWith(separator))
            {
                path = path.substring(0, path.length() - separator.length());
                k++;
            }
            if (k > 0)
            {
                char[] separatorChars = separator.toCharArray();
                char[] value = new char[path.length() + (separatorChars.length + 1) * k];
                System.arraycopy(path.toCharArray(), 0, value, 0, path.length());
                for (int offset = path.length(); offset < value.length; offset += separatorChars.length + 1)
                {
                    System.arraycopy(separatorChars, 0, value, offset, separatorChars.length);
                    value[offset + separatorChars.length] = '_';
                }
                path = new String(value);
            }
            
            return path;
        }
    }
    
    class TVShowFileSystemStorageUnit implements StorageUnit
    {
        
        private Path parent, file;
        private SeekableByteChannel sbc;
        private long capacity;
        
        private volatile boolean closed;
        
        TVShowFileSystemStorageUnit(Path root, String path, long capacity)
        {
            this.file = root.resolve(path);
            this.parent = file.getParent();
            this.capacity = capacity;
            this.closed = true;
        }
        
        // TODO: this is temporary fix for verification upon app start
        // should be re-done (probably need additional API to know if storage unit is "empty")
        private boolean init(boolean create)
        {
            if (closed)
            {
                if (!Files.exists(file))
                {
                    if (create)
                    {
                        if (!Files.exists(parent))
                        {
                            try
                            {
                                Files.createDirectories(parent);
                            }
                            catch (IOException e)
                            {
                                throw new BtException("Failed to create file storage -- can't create (some of the) directories", e);
                            }
                        }
                        
                        try
                        {
                            Files.createFile(file);
                        }
                        catch (IOException e)
                        {
                            throw new BtException("Failed to create file storage -- " + "can't create new file: " + file.toAbsolutePath(), e);
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
                
                try
                {
                    sbc = Files.newByteChannel(file, StandardOpenOption.READ, StandardOpenOption.WRITE);
                }
                catch (IOException e)
                {
                    throw new BtException("Unexpected I/O error", e);
                }
                
                closed = false;
            }
            return true;
        }
        
        @Override
        public synchronized void readBlock(ByteBuffer buffer, long offset)
        {
            
            if (closed)
            {
                if (!init(false)) return;
            }
            
            if (offset < 0) throw new BtException("Illegal arguments: offset (" + offset + ")");
            else if (offset > capacity - buffer.remaining())
            {
                throw new BtException("Received a request to read past the end of file (offset: " + offset + ", requested block length: " + buffer.remaining() + ", file size: " + capacity);
            }
            
            try
            {
                sbc.position(offset);
                int read = 1;
                while (buffer.hasRemaining() && read > 0) read = sbc.read(buffer);
                
            }
            catch (IOException e)
            {
                throw new BtException("Failed to read bytes (offset: " + offset + ", requested block length: " + buffer.remaining() + ", file size: " + capacity + ")", e);
            }
        }
        
        @Override
        public synchronized byte[] readBlock(long offset, int length)
        {
            
            if (closed)
            {
                if (!init(false))
                {
                    // TODO: should we return null here? or init this "stub" in constructor?
                    return new byte[length];
                }
            }
            
            if (offset < 0 || length < 0)
            {
                throw new BtException("Illegal arguments: offset (" + offset + "), length (" + length + ")");
            }
            else if (offset > capacity - length)
            {
                throw new BtException("Received a request to read past the end of file (offset: " + offset + ", requested block length: " + length + ", file size: " + capacity);
            }
            
            try
            {
                sbc.position(offset);
                ByteBuffer buf = ByteBuffer.allocate(length);
                int read = 1;
                while (buf.hasRemaining() && read > 0) read = sbc.read(buf);
                
                return buf.array();
                
            }
            catch (IOException e)
            {
                throw new BtException("Failed to read bytes (offset: " + offset + ", requested block length: " + length + ", file size: " + capacity + ")", e);
            }
        }
        
        @Override
        public synchronized void writeBlock(ByteBuffer buffer, long offset)
        {
            
            if (closed) init(true);
            
            if (offset < 0) throw new BtException("Negative offset: " + offset);
            else if (offset > capacity - buffer.remaining())
            {
                throw new BtException("Received a request to write past the end of file (offset: " + offset + ", block length: " + buffer.remaining() + ", file size: " + capacity);
            }
            
            try
            {
                sbc.position(offset);
                int written = 1;
                while (buffer.hasRemaining() && written > 0) written = sbc.write(buffer);
                
            }
            catch (IOException e)
            {
                throw new BtException("Failed to write bytes (offset: " + offset + ", block length: " + buffer.remaining() + ", file size: " + capacity + ")", e);
            }
        }
        
        @Override
        public synchronized void writeBlock(byte[] block, long offset)
        {
            
            if (closed) init(true);
            
            if (offset < 0) throw new BtException("Negative offset: " + offset);
            else if (offset > capacity - block.length)
            {
                throw new BtException("Received a request to write past the end of file (offset: " + offset + ", block length: " + block.length + ", file size: " + capacity);
            }
            
            try
            {
                sbc.position(offset);
                ByteBuffer buf = ByteBuffer.wrap(block);
                int written = 1;
                while (buf.hasRemaining() && written > 0) written = sbc.write(buf);
                
            }
            catch (IOException e)
            {
                throw new BtException("Failed to write bytes (offset: " + offset + ", block length: " + block.length + ", file size: " + capacity + ")", e);
            }
        }
        
        @Override
        public long capacity()
        {
            return capacity;
        }
        
        @Override
        public long size()
        {
            
            try
            {
                return Files.exists(file) ? Files.size(file) : 0;
            }
            catch (IOException e)
            {
                throw new BtException("Unexpected I/O error", e);
            }
        }
        
        @Override
        public String toString()
        {
            return "(" + capacity + " B) " + file;
        }
        
        @Override
        public void close() throws IOException
        {
            if (!closed)
            {
                try
                {
                    sbc.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    closed = true;
                }
            }
        }
    }
}