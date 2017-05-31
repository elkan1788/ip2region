package io.github.elkan1788.ip2region;

/**
 * configuration exception
 * 
 * @author chenxin(chenxin619315@gmail.com)
 * @author 凡梦星尘(elkan1788@gmail.com)
 */
public class IPSearcherException extends Exception {

    public IPSearcherException(String info) {
        super(info);
    }

    public IPSearcherException(Throwable res) {
        super(res);
    }

    public IPSearcherException(String info, Throwable res) {
        super(info, res);
    }
}
