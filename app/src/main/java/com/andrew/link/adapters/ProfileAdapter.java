package com.andrew.link.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.andrew.link.fragments.DocumentFragment;
import com.andrew.link.fragments.GalleryFragment;
import com.andrew.link.fragments.MessageFragment;
import com.andrew.link.fragments.VoiceFragment;

public class ProfileAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public ProfileAdapter(FragmentManager fm, int NoofTabs) {
        super(fm);
        this.mNumOfTabs = NoofTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                MessageFragment messageFragment = new MessageFragment();
                return messageFragment;
            case 1:
                GalleryFragment galleryFragment = new GalleryFragment();
                return galleryFragment;
            case 2:
                DocumentFragment documentFragment = new DocumentFragment();
                return documentFragment;
            case 3:
                VoiceFragment voiceFragment = new VoiceFragment();
                return voiceFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
