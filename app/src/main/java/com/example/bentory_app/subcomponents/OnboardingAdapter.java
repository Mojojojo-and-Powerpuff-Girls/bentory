package com.example.bentory_app.subcomponents;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.OnboardingPage;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    // List of onboarding pages to display.
    private List<OnboardingPage> onboardingPages;

    public OnboardingAdapter(List<OnboardingPage> onboardingPages) {
        this.onboardingPages = onboardingPages;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate each onboarding page layout.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        // Bind current onboarding page data to view.
        holder.bind(onboardingPages.get(position));
    }

    @Override
    public int getItemCount() {
        // Return total number of onboarding screens.
        return onboardingPages.size();
    }

    // ViewHolder for onboarding items.
    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.onboardingImage);
        }

        // Binds the onboardingPage data to the ImageView.
        void bind(OnboardingPage page) {
            imageView.setImageResource(page.getImageResId());
        }

    }

}
