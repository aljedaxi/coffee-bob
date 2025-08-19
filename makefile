serve:
	nix shell nixpkgs#leiningen --command lein ring server-headless

out:
	nix shell nixpkgs#leiningen --command lein run -m coffee-bob.export
