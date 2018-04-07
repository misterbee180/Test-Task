package com.example.testtask;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.InterruptedIOException;
import java.util.ArrayList;

/**
 * Created by Misterbee180 on 7/16/2017.
 */

public class ArrayListContainer {

    ArrayList<String> mArrayList;
    ArrayList<Decoder> mDecoderList;
    ArrayAdapter<String> mAdapter;
    ListView mListView;
    Spinner mSpinner;

    public ArrayListContainer(){
        mArrayList = new ArrayList<String>();
        mDecoderList = new ArrayList<Decoder>();
    }

    //This function exists with the idea that this might link an adapter to other viewers instead of a List View
    public void LinkArrayToListView(ListView pListView,
                                    Context pContext){
        mListView = pListView;
        mAdapter = new ArrayAdapter<String>(pContext,android.R.layout.simple_list_item_1, mArrayList);
        mListView.setAdapter(mAdapter);
    }

    public void LinkArrayToSpinner(Spinner pSpinner,
                                   Context pContext){
        mSpinner = pSpinner;
        mAdapter = new ArrayAdapter<String>(pContext,android.R.layout.simple_spinner_item, mArrayList);
        mSpinner.setAdapter(mAdapter);
    }

    public void Add(String pstrListItem,
                    Long plngIndex){
        Integer itemNumber = mArrayList.size();
        mDecoderList.add(new Decoder(itemNumber, plngIndex));
        mArrayList.add(pstrListItem);
    }

    public void Remove(Integer plngLocation){
        //Everything with a Item Number > than the item number passed in needs to be moved down one.
        Decoder tmpDecoder;
        for (int i = 0; i < mDecoderList.size(); i++){
            tmpDecoder = mDecoderList.get(i);
            if (tmpDecoder.mItemNumber > plngLocation){
                tmpDecoder.mItemNumber--;
            }
        }
        mArrayList.remove(plngLocation);
    }

    //This function could just be replaced with an array of Ints. We just need to make sure that this list is ordered the same way as the array list.
    public long GetID(Integer plngLocation){
        Decoder tmpDecoder;
        for (int i = 0; i < mDecoderList.size(); i++){
            tmpDecoder = mDecoderList.get(i);
            if (tmpDecoder.mItemNumber == plngLocation){
                return tmpDecoder.mIndexNumber;
            }
        }
        return (long)-1;
    }

    public void setIDListView(Long plngID){
        Decoder tmpDecoder;
        for (int i = 0; i < mDecoderList.size(); i++){
            tmpDecoder = mDecoderList.get(i);
            if (tmpDecoder.mIndexNumber == plngID){
                mListView.setSelection(tmpDecoder.mItemNumber);
            }
        }
    }

    public void setIDSpinner(Long plngID){
        Decoder tmpDecoder;
        for (int i = 0; i < mDecoderList.size(); i++){
            tmpDecoder = mDecoderList.get(i);
            if (tmpDecoder.mIndexNumber == plngID){
                mSpinner.setSelection(tmpDecoder.mItemNumber);
            }
        }
    }

    public void Clear(){
        mArrayList.clear();
        mDecoderList.clear();
    }

    private class Decoder{
        Integer mItemNumber;
        Long mIndexNumber;

        public Decoder(Integer plngItemNumber,
                            Long plngIndexNumber){
            mIndexNumber = plngIndexNumber;
            mItemNumber = plngItemNumber;
        }
    }
}
