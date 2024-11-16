package tgw.evolution.entities.util;

public interface IWrapCallback {

    IWrapCallback DUMMY = new IWrapCallback() {

        @Override
        public void onX2NegativeWrap() {
        }

        @Override
        public void onX2PositiveWrap() {
        }

        @Override
        public void onZ2NegativeWrap() {
        }

        @Override
        public void onZ2PositiveWrap() {
        }
    };

    void onX2NegativeWrap();

    void onX2PositiveWrap();

    void onZ2NegativeWrap();

    void onZ2PositiveWrap();
}
